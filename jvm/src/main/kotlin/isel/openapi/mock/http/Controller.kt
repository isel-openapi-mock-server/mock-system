package isel.openapi.mock.http

import isel.openapi.mock.parsingServices.Parsing
import isel.openapi.mock.services.DynamicRoutesServices
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class DynamicRouteController(
    private val dynamicRoutesServices: DynamicRoutesServices,
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
        val res = dynamicRoutesServices.addDynamicRoute(apiSpec)
        return ResponseEntity.ok(res)
    }
}

data class OpenApiSpec(val spec: String)



