package isel.openapi.mock.services

import isel.openapi.mock.domain.dynamic.DynamicDomain
import isel.openapi.mock.domain.dynamic.HandlerResult
import isel.openapi.mock.domain.problems.ProblemsDomain
import isel.openapi.mock.parsingServices.Parsing
import isel.openapi.mock.parsingServices.model.HttpMethod
import isel.openapi.mock.parsingServices.model.Response
import isel.openapi.mock.repository.TransactionManager
import isel.openapi.mock.utils.Either
import isel.openapi.mock.utils.failure
import isel.openapi.mock.utils.success
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

sealed interface DynamicHandlerError {
    data class NotFound(val message: String) : DynamicHandlerError
}

typealias DynamicHandlerResult = Either<DynamicHandlerError, Pair<Response, String>>

@Component
class DynamicHandlerServices(
    private val router: Router,
    private val problemsDomain: ProblemsDomain,
    private val transactionManager: TransactionManager,
) {

    fun executeDynamicHandler(
        host: String,
        method: HttpMethod,
        path: String,
        request: HttpServletRequest
    ) : DynamicHandlerResult {

        val dynamicHandler = router.match(host, method, path)
            ?: return failure(DynamicHandlerError.NotFound("No handler found for $method $path"))

        val handlerResponse : HandlerResult = dynamicHandler.first?.handle(request) ?: return failure(
            DynamicHandlerError.NotFound("No handler found for $method $path")
        )

        val requestUuid = problemsDomain.generateUuidValue()
        val fails = handlerResponse.fails

        transactionManager.run{
            val problemsRepository = it.problemsRepository

            problemsRepository.addRequest(
                requestUuid,
                dynamicHandler.second,
                method.name,
                path,
                "todo-external-key", //TODO: add external key
                host
            )

            if(handlerResponse.body != null) {
                problemsRepository.addRequestBody(requestUuid, handlerResponse.body.toByteArray(), handlerResponse.headers["content-type"] ?: "")
            }

            if(handlerResponse.headers.isNotEmpty()) {
                problemsRepository.addRequestHeaders(requestUuid, handlerResponse.headers)
            }

            if(handlerResponse.params.isNotEmpty()) {
                problemsRepository.addRequestParams(requestUuid, handlerResponse.params)
            }

            if(fails.isNotEmpty()) {
                problemsRepository.addProblems(requestUuid, fails)
            }

            val responseId = problemsRepository.addResponse(requestUuid, handlerResponse.responseInfo.response.statusCode.code)

            if(handlerResponse.responseInfo.headers.isNotEmpty()) {
                problemsRepository.addResponseHeaders(responseId, handlerResponse.responseInfo.headers)
            }

            if(handlerResponse.responseInfo.body != null) {
                problemsRepository.addResponseBody(responseId, handlerResponse.responseInfo.body.toByteArray(), handlerResponse.responseInfo.response.schema.toString())
            }
        }

        return success(
            Pair(
                handlerResponse.responseInfo.response,
                requestUuid
            )
        )

    }

}