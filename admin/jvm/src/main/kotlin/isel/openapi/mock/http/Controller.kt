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
    ): ResponseEntity<*> {
        return ResponseEntity.ok("Admin application started")
    }

}

data class OpenApiSpec(val spec: String)



