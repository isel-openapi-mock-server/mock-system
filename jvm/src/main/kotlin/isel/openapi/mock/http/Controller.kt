package isel.openapi.mock.http

import isel.openapi.mock.services.DynamicRoutesServices
import isel.openapi.mock.utils.extractApiSpec
import isel.openapi.mock.utils.parseOpenApi
import isel.openapi.mock.utils.validateOpenApi
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method
import org.springframework.web.servlet.mvc.method.RequestMappingInfo

import java.util.concurrent.ConcurrentHashMap

@RestController
class DynamicRouteController(
    private val dynamicRoutesServices: DynamicRoutesServices
) {
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello, World!"
    }
/*
    @PostMapping("/new")
    fun addDynamicRoute(@RequestBody newRoute: NewRoute): String {
        val method = when (newRoute.method.uppercase()) {
            "GET" -> RequestMethod.GET
            "POST" -> RequestMethod.POST
            "PUT" -> RequestMethod.PUT
            "DELETE" -> RequestMethod.DELETE
            else -> throw IllegalArgumentException("Unsupported method: ${newRoute.method}")
        }

        val handler = DynamicHandler(newRoute.response)

        val methodInstance: Method = DynamicHandler::class.java.getMethod("handle")

        val mappingInfo = RequestMappingInfo
            .paths(newRoute.path)
            .methods(method)
            .build()

        handlerMapping.registerMapping(mappingInfo, handler, methodInstance)
        dynamicRoutes[newRoute.path] = mappingInfo

        return "Route added: ${newRoute.path} [${newRoute.method}]"
    }

 */
/*
    @PostMapping("/remove-route")
    fun removeDynamicRoute(@RequestBody removeRoute: RemoveRoute): String {
        val mappingInfo = dynamicRoutes.remove(removeRoute.path)
        return if (mappingInfo != null) {
            handlerMapping.unregisterMapping(mappingInfo)
            "Route removed: ${removeRoute.path} [${removeRoute.method}]"
        } else {
            "Route not found: ${removeRoute.path}"
        }
    }

 */

    @PostMapping("/openapi")
    fun addOpenApiSpec(
        @RequestBody openApiSpec: OpenApiSpec
    ): ResponseEntity<*> {
        if(!validateOpenApi(openApiSpec.spec)) {
            return ResponseEntity.badRequest().body("Invalid OpenAPI Spec")
        }
        val openApi = parseOpenApi(openApiSpec.spec)
            ?: return ResponseEntity.badRequest().body("Invalid OpenAPI Spec")
        val apiSpec = extractApiSpec(openApi)
        val res = dynamicRoutesServices.addDynamicRoute(apiSpec)
        return ResponseEntity.ok(res)
    }
}

data class NewRoute(val path: String, val method: String, val response: String)
data class RemoveRoute(val path: String, val method: String)
data class OpenApiSpec(val spec: String)



