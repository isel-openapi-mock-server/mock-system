package isel.openapi.mock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method
import org.springframework.web.servlet.mvc.method.RequestMappingInfo

import java.util.concurrent.ConcurrentHashMap

@RestController
class DynamicRouteController(@Autowired val context: ApplicationContext) {

    @Autowired
    private lateinit var handlerMapping: RequestMappingHandlerMapping

    private val dynamicRoutes = ConcurrentHashMap<String, RequestMappingInfo>()

    @GetMapping("/hello")
    fun hello(): String {
        return "Hello, World!"
    }

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
}

data class NewRoute(val path: String, val method: String, val response: String)
data class RemoveRoute(val path: String, val method: String)

class DynamicHandler(private val response: String) {
    @ResponseBody
    fun handle(): String {
        return response
    }
}

