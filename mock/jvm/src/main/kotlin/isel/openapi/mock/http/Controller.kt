package isel.openapi.mock.http

import isel.openapi.mock.domain.openAPI.HttpMethod
import isel.openapi.mock.http.model.Problem
import isel.openapi.mock.services.DynamicHandlerError
import isel.openapi.mock.services.DynamicHandlerServices
import isel.openapi.mock.utils.Failure
import isel.openapi.mock.utils.Success
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

const val EXCHANGE_KEY_HEADER = "Exchange-Key"

@RestController
class DynamicRouteController(
    private val services: DynamicHandlerServices
) {
    @RequestMapping(
        value = ["/**"],
        method = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE]
    )
    fun handleDynamicRequest(
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val method = when (request.method) {
            "GET" -> HttpMethod.GET
            "POST" -> HttpMethod.POST
            "PUT" -> HttpMethod.PUT
            "DELETE" -> HttpMethod.DELETE
            else -> return ResponseEntity.badRequest().body("Unsupported method")
        }
        val host = request.getHeader("Host")
            ?: return ResponseEntity.badRequest().body("Host header is missing")
        val externalKey = request.getHeader("External-Key")
        val scenarioName: String = request.getHeader("Scenario-name")
            ?: return ResponseEntity.badRequest().body("Scenario-name header is missing")
        val res = services.executeDynamicHandler(
            host = host,
            method = method,
            path = request.requestURI,
            request = request,
            externalKey = externalKey,
            scenarioName = scenarioName
        )
        return when (res) {
            is Success -> {
                val resp = ResponseEntity.status(res.value.first.statusCode.code)
                    .header(EXCHANGE_KEY_HEADER, res.value.second)

                res.value.first.headers?.forEach { header ->
                    resp.header(header.key, header.value)
                }

                resp.body(res.value.first.body)

            }
            is Failure -> {
                when (res.value) {
                    DynamicHandlerError.HandlerNotFound ->
                        Problem.response(404, Problem.handlerNotFound)
                    DynamicHandlerError.HostDoesNotExist ->
                        Problem.response(400, Problem.hostDoesNotExist)
                    DynamicHandlerError.ScenarioNotFound ->
                        Problem.response(404, Problem.scenarioNotFound)
                    is  DynamicHandlerError.NoResponseForThisRequestInScenario ->
                        Problem.response(404, Problem.noResponseForThisRequestInScenario)
                    DynamicHandlerError.NoResponseForThisRequest ->
                        Problem.response(404, Problem.noResponseForThisRequest)
                    is DynamicHandlerError.BadRequest ->
                        Problem.response(400, Problem.badRequest, res.value.exchangeKey)
                }
            }
        }
    }

    @PostMapping("/dynamic-routes/update")
    fun updateDynamicRoutes(): ResponseEntity<*> {
        services.updateDynamicRouter()
        return ResponseEntity.ok().body("Dynamic routes updated successfully")
    }

}
