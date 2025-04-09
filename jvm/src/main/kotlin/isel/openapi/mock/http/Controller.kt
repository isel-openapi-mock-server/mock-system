package isel.openapi.mock.http

import isel.openapi.mock.parsingServices.Parsing
import isel.openapi.mock.parsingServices.model.HttpMethod
import isel.openapi.mock.services.Router
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class DynamicRouteController(
    private val router: Router,
    private val parsing: Parsing
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
        val handler = router.match("mock", method, request.requestURI)
            ?:
            return ResponseEntity
                .badRequest()
                .body("No matching route found for ${request.requestURI} with method ${request.method}")
        return handler.handle(request)
    }

}

data class OpenApiSpec(val spec: String)



