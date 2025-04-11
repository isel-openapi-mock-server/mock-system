package isel.openapi.mock.services

import isel.openapi.mock.domain.ProblemsDomain
import isel.openapi.mock.parsingServices.model.HttpMethod
import isel.openapi.mock.parsingServices.model.Response
import isel.openapi.mock.utils.Either
import isel.openapi.mock.utils.failure
import isel.openapi.mock.utils.success
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

sealed interface DynamicHandlerError {
    data class NotFound(val message: String) : DynamicHandlerError
}

typealias DynamicHandlerResult = Either<DynamicHandlerError, Response>

@Component
class DynamicHandlerServices(
    private val router: Router,
    private val problemsDomain: ProblemsDomain,
) {

    fun executeDynamicHandler(
        host: String,
        method: HttpMethod,
        uri: String,
        request: HttpServletRequest
    ) : DynamicHandlerResult {

        val dynamicHandler = router.match(host, method, uri)
            ?: return failure(DynamicHandlerError.NotFound("No handler found for $method $uri"))

        val handlerResponse = dynamicHandler.first?.handle(request) ?: return failure(
            DynamicHandlerError.NotFound("No handler found for $method $uri")
        )

        val requestUuid = problemsDomain.generateUuidValue()
        val fails = handlerResponse.fails

        //TODO: guardar os fails e gerar o uuid do request
        //TODO: guardar o uuid do request para enviar num header
        return success(handlerResponse.response)

    }

}