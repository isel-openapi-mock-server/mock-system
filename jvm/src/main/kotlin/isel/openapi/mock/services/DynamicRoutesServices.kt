package isel.openapi.mock.services

import isel.openapi.mock.http.DynamicHandler
import isel.openapi.mock.http.OpenApiSpec
import isel.openapi.mock.repository.DynamicRoutesMem
import isel.openapi.mock.utils.ApiSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method
import kotlin.collections.set

@Component
class DynamicRoutesServices(
    private val dynamicRoutesMem: DynamicRoutesMem
) {

    @Autowired
    private lateinit var handlerMapping: RequestMappingHandlerMapping

    fun addDynamicRoute(apiSpec: ApiSpec): String {
        apiSpec.paths.forEach { apiPath ->
            apiPath.methods.forEach { apiMethod ->
                val method = when (apiMethod.method.uppercase()) {
                    "GET" -> RequestMethod.GET
                    "POST" -> RequestMethod.POST
                    "PUT" -> RequestMethod.PUT
                    "DELETE" -> RequestMethod.DELETE
                    else -> throw IllegalArgumentException("Unsupported method: ${apiMethod.method}")
                }

                val handler = DynamicHandler(apiMethod.responses[0].statusCode)

                val methodInstance: Method = DynamicHandler::class.java.getMethod("handle")

                val mappingInfo = RequestMappingInfo
                    .paths(apiPath.path)
                    .methods(method)
                    .build()

                handlerMapping.registerMapping(mappingInfo, handler, methodInstance)
                dynamicRoutesMem.addDynamicRoute(apiPath.path, apiMethod)
            }
        }
        return "Route added"
    }

}