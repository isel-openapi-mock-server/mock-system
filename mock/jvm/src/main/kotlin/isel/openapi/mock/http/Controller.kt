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
            else -> return Problem.response(500, Problem.unsupportedMethod)
        }
        val host = request.getHeader("Host")
            ?: return Problem.response(400, Problem.hostHeader)
        val externalKey = request.getHeader("External-Key")
        val scenarioName: String = request.getHeader("Scenario-name")
            ?: return Problem.response(400, Problem.scenarioHeader)
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
                val resp = ResponseEntity.status(res.value.statusCode.code)
                    .header(EXCHANGE_KEY_HEADER, res.value.exchangeKey)

                res.value.headers?.forEach { header ->
                    resp.header(header.key, header.value)
                }

                resp.header("Content-Type", res.value.contentType)

                resp.body(res.value.body)

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

    /**
     * Rota adicionada para fins de testes
     */
    @PostMapping("/dynamic-routes/update")
    fun updateDynamicRoutes(): ResponseEntity<*> {
        services.updateDynamicRouter()
        return ResponseEntity.ok().body("Dynamic routes updated successfully")
    }

}
