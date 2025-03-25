package isel.openapi.mock.services

import isel.openapi.mock.http.BasicDynamicHandler
import isel.openapi.mock.http.BodyDynamicHandler
import isel.openapi.mock.http.DynamicHandler
import isel.openapi.mock.http.ParamsDynamicHandler
import isel.openapi.mock.repository.DynamicRoutesMem
import isel.openapi.mock.utils.ApiSpec
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

@Component
class DynamicRoutesServices(
    private val dynamicRoutesMem: DynamicRoutesMem
) {

    //Request mapping handler mapping é uma classe do spring que mapeia os endpoints para os handlers
    //Contem todos os endpoints mapeados e os handlers associados na aplicação
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

                var methodInstance: Method
                var handler: DynamicHandler

                if(apiMethod.parameters.isNotEmpty()) {
                    handler = ParamsDynamicHandler(apiMethod.responses[0].statusCode, apiMethod.parameters)
                    methodInstance = ParamsDynamicHandler::class.java.getMethod(
                        "handle", Map::class.java
                    )
                } else if(apiMethod.requestBody != null) {
                    handler = BodyDynamicHandler(apiMethod.responses[0].statusCode, apiMethod.requestBody)
                    methodInstance = BodyDynamicHandler::class.java.getMethod(
                        "handle", HttpServletRequest::class.java
                    )
                } else {
                    handler = BasicDynamicHandler(apiMethod.responses[0].statusCode)
                    methodInstance = BasicDynamicHandler::class.java.getMethod(
                        "handle"
                    )
                }

                val mappingInfo = RequestMappingInfo
                    .paths(apiPath.path)
                    .methods(method)
                    .build()

                //Registar o mapeamento do endpoint com o handler
                /**
                 * @param mappingInfo - Informação do mapeamento
                 * @param handler - Handler que vai processar o pedido
                 * @param methodInstance - Método que vai ser chamado no handler
                 */
                handlerMapping.registerMapping(mappingInfo, handler, methodInstance)

                dynamicRoutesMem.addDynamicRoute(apiPath.path, apiMethod)
            }
        }
        return "Route added"
    }

}