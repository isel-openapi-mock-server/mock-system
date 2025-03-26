package isel.openapi.mock.services

import isel.openapi.mock.http.BodyAndParamsDynamicHandler
import isel.openapi.mock.repository.DynamicRoutesMem
import isel.openapi.mock.parsingServices.model.ApiSpec
import isel.openapi.mock.parsingServices.model.HttpMethod
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

@Component
class DynamicRoutesServices(
    private val dynamicRoutesMem: DynamicRoutesMem,
    private val handlerMapping: RequestMappingHandlerMapping
) {

    //Request mapping handler mapping é uma classe do spring que mapeia os endpoints para os handlers
    //Contem todos os endpoints mapeados e os handlers associados na aplicação

    fun addDynamicRoute(apiSpec: ApiSpec): String {
        apiSpec.paths.forEach { apiPath ->
            apiPath.operations.forEach { operation ->
                val method = when (operation.method) {
                    HttpMethod.GET -> RequestMethod.GET
                    HttpMethod.POST -> RequestMethod.POST
                    HttpMethod.PUT -> RequestMethod.PUT
                    HttpMethod.DELETE -> RequestMethod.DELETE
                    else -> throw IllegalArgumentException("Unsupported method: ${operation.method}")
                }

                val handler = BodyAndParamsDynamicHandler(
                    operation.responses[0].statusCode.code.toString(),
                    operation.parameters,
                    operation.requestBody
                )
                val methodInstance: Method = BodyAndParamsDynamicHandler::class.java.getMethod(
                    "handle", HttpServletRequest::class.java
                )

                val mappingInfo = RequestMappingInfo
                    .paths(apiPath.fullPath)
                    .methods(method)
                    .build()

                //Registar o mapeamento do endpoint com o handler
                /**
                 * @param mappingInfo - Informação do mapeamento
                 * @param handler - Handler que vai processar o pedido
                 * @param methodInstance - Método que vai ser chamado no handler
                 */
                handlerMapping.registerMapping(mappingInfo, handler, methodInstance)

                dynamicRoutesMem.addDynamicRoute(apiPath.fullPath, operation)
            }
        }
        return "Route added"
    }

}