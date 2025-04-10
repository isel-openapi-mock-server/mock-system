package isel.openapi.mock.http

import isel.openapi.mock.parsingServices.Parsing
import isel.openapi.mock.parsingServices.model.HttpMethod
import isel.openapi.mock.services.DynamicHandlerError
import isel.openapi.mock.services.DynamicHandlerServices
import isel.openapi.mock.services.Router
import isel.openapi.mock.utils.Failure
import isel.openapi.mock.utils.Success
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class DynamicRouteController(
    private val router: Router, //TODO: tirar
    private val parsing: Parsing,
    private val services: DynamicHandlerServices
) {
    @PostMapping("/openapi")
    fun addOpenApiSpec(
        @RequestBody openApiSpec: OpenApiSpec
    ): ResponseEntity<*> {
        if(!parsing.validateOpenApi(openApiSpec.spec)) {
            return ResponseEntity.badRequest().body("Invalid OpenAPI Spec")
        }
        val openApi = parsing.parseOpenApi(openApiSpec.spec)
            ?: return ResponseEntity.badRequest().body("Invalid OpenAPI Spec")
        val apiSpec = parsing.extractApiSpec(openApi)
        val res = router.register(apiSpec)
        return ResponseEntity.ok(res)
    }

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
        val res = services.executeDynamicHandler(
            host = request.serverName,
            method = method,
            uri = request.requestURI,
            request = request
        )
        return when (res) {
            is Success -> {
                ResponseEntity.status(res.value.statusCode.code).build<Unit>()
            }
            is Failure -> {
                when (res.value) {
                    is DynamicHandlerError.NotFound -> {
                        ResponseEntity.notFound().build<Unit>()
                    }
                }
            }
        }
    }
}

data class OpenApiSpec(val spec: String)
