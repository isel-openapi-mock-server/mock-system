package isel.openapi.mock.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import isel.openapi.mock.domain.dynamic.HandlerResult
import isel.openapi.mock.domain.problems.ProblemsDomain
import isel.openapi.mock.parsingServices.model.*
import isel.openapi.mock.repository.TransactionManager
import isel.openapi.mock.utils.Either
import isel.openapi.mock.utils.failure
import isel.openapi.mock.utils.success
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

sealed interface DynamicHandlerError {
    data object NotFound: DynamicHandlerError
    data object HostDoesNotExist: DynamicHandlerError
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

        var dynamicHandler = router.match(host, method, path)

        if(dynamicHandler == null) {
            val spec = uploadOpenAPI(host) ?: return failure(DynamicHandlerError.HostDoesNotExist)
            router.register(spec, host)
            dynamicHandler = router.match(host, method, path)
        }

        val handlerResponse : HandlerResult = dynamicHandler!!.first?.handle(request) ?: return failure(
            DynamicHandlerError.NotFound
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

    fun uploadOpenAPI(
        host: String,
    ) : ApiSpec? {

        val openAPI = transactionManager.run {
            val openAPIRepository = it.openAPIRepository
            openAPIRepository.uploadOpenAPI(host)
        }

        if(openAPI == null) {
            return null
        }

        val paths = mutableListOf<ApiPath>()

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        openAPI.paths.forEach { apiPath ->

            val operations : List<PathOperation> = mapper.readValue(apiPath.operations, object : TypeReference<List<PathOperation>>() {})

            paths.add(ApiPath(
                fullPath = apiPath.path,
                operations = operations,
                path = splitPath(apiPath.path)
            ))

        }

        return ApiSpec(
            name = openAPI.name,
            description = openAPI.description,
            paths = paths
        )
    }

    private fun splitPath(
        path: String
    ): List<PathParts> {
        return path.split("/").filter { it.isNotBlank() }.map { part ->
            if (part.startsWith("{") && part.endsWith("}")) {
                val paramName = part.substring(1, part.length - 1)
                PathParts(paramName, true)
            } else {
                PathParts(part, false)
            }
        }
    }

}