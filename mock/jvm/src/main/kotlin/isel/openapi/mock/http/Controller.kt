package isel.openapi.mock.http

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
    private val router: Router,
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
        val host = request.getHeader("Host") ?: return ResponseEntity.badRequest().body("Host header is missing")
        println("Host: $host")
        val res = services.executeDynamicHandler(
            host = host,
            method = method,
            path = request.requestURI,
            request = request
        )
        return when (res) {
            is Success -> {
                ResponseEntity.status(res.value.first.statusCode.code)
                    .header("Request-Key", res.value.second)
                    .build<Unit>()
            }
            is Failure -> {
                when (res.value) {
                    is DynamicHandlerError.NotFound -> {
                        ResponseEntity.notFound().build<Unit>()
                    }
                    is DynamicHandlerError.HostDoesNotExist -> {
                        ResponseEntity.badRequest().body("Host does not exist")
                    }
                }
            }
        }
    }
}

data class OpenApiSpec(val spec: String)
