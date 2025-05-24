package isel.openapi.mock.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import isel.openapi.mock.domain.dynamic.HandlerResult
import isel.openapi.mock.domain.dynamic.RouteNode
import isel.openapi.mock.domain.openAPI.*
import isel.openapi.mock.domain.problems.ProblemsDomain
import isel.openapi.mock.repository.TransactionManager
import isel.openapi.mock.utils.Either
import isel.openapi.mock.utils.failure
import isel.openapi.mock.utils.success
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

sealed interface DynamicHandlerError {
    data object NotFound: DynamicHandlerError
    data object HostDoesNotExist: DynamicHandlerError
    data object ScenarioNotFound: DynamicHandlerError
    data object NoResponseForThisRequestInScenario : DynamicHandlerError // Para quando nao as resposta do scenario nao forem para aquele pedido
}

typealias DynamicHandlerResult = Either<DynamicHandlerError, Pair<ResponseConfig, String>>

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
        request: HttpServletRequest,
        externalKey: String? = null,
        scenarioName: String,
    ) : DynamicHandlerResult {

        if (!router.doesHostExist(host)) return failure(DynamicHandlerError.HostDoesNotExist)

        if (!router.doesScenarioExist(host, scenarioName)) return failure(DynamicHandlerError.ScenarioNotFound)

        val dynamicHandler = router.match(host, method, path) ?: return failure(DynamicHandlerError.NotFound)

        dynamicHandler.scenarios.find { it.name == scenarioName } ?: return failure(DynamicHandlerError.NoResponseForThisRequestInScenario)

        val handlerResponse : HandlerResult = dynamicHandler.dynamicHandler?.handle(request, scenarioName) ?: return failure(
            DynamicHandlerError.NotFound
        )

        val requestUuid = problemsDomain.generateUuidValue()
        val fails = handlerResponse.fails

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        transactionManager.run{
            val problemsRepository = it.problemsRepository

            val jsonRequestHeaders =
                if(handlerResponse.headers.isNotEmpty()) mapper.writeValueAsString(handlerResponse.headers)
                else null

            problemsRepository.addRequest(
                requestUuid,
                dynamicHandler.resourceUrl,
                method.name,
                path,
                externalKey,
                host,
                jsonRequestHeaders,
            )

            if(handlerResponse.body != null) {
                problemsRepository.addRequestBody(requestUuid, handlerResponse.body.toByteArray(), handlerResponse.headers["content-type"] ?: "")
            }

            if(handlerResponse.params.isNotEmpty()) {
                problemsRepository.addRequestParams(requestUuid, handlerResponse.params)
            }

            if(fails.isNotEmpty()) {
                problemsRepository.addProblems(requestUuid, fails)
            }

            val jsonResponseHeaders =
                if(handlerResponse.headers.isNotEmpty()) mapper.writeValueAsString(handlerResponse.headers)
                else null

            val responseId = problemsRepository.addResponse(requestUuid, handlerResponse.response.statusCode.code, jsonResponseHeaders)

            if(handlerResponse.response.body != null) {
                problemsRepository.addResponseBody(responseId, handlerResponse.response.body, handlerResponse.headers["content-type"] ?: "")
            }
        }

        return success(
            Pair(
                handlerResponse.response,
                requestUuid
            )
        )

    }

    fun updateDynamicRoutes() {

        val newMap = mutableMapOf<String, RouteNode>()

        val specs = transactionManager.run {
            val openAPIRepository = it.openAPIRepository
            openAPIRepository.uploadOpenAPI()
        }

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        for( (host, openAPI) in specs) {

            val paths = mutableListOf<ApiPath>()

            openAPI.paths.forEach { apiPath ->

                val operations : List<PathOperation> = mapper.readValue(apiPath.operations, object : TypeReference<List<PathOperation>>() {})

                paths.add(
                    ApiPath(
                        fullPath = apiPath.path,
                        operations = operations,
                        path = splitPath(apiPath.path)
                    )
                )

            }

            val routeNode = router.createRouterNode(
                ApiSpec(
                    name = openAPI.name,
                    description = openAPI.description,
                    paths = paths
                ),
                host
            )

            newMap[host] = routeNode

        }

        router.register(newMap)

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