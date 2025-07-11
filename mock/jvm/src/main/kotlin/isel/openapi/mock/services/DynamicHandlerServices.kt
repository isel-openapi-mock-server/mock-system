package isel.openapi.mock.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.jknack.handlebars.Handlebars
import isel.openapi.mock.domain.dynamic.HandlerResult
import isel.openapi.mock.domain.dynamic.ProcessedRequest
import isel.openapi.mock.domain.dynamic.RouteNode
import isel.openapi.mock.domain.openAPI.*
import isel.openapi.mock.domain.problems.ProblemsDomain
import isel.openapi.mock.repository.TransactionManager
import isel.openapi.mock.utils.Either
import isel.openapi.mock.utils.failure
import isel.openapi.mock.utils.success
import jakarta.servlet.http.HttpServletRequest
import kotlinx.datetime.Clock
import org.springframework.stereotype.Component
import org.apache.commons.text.StringEscapeUtils

sealed interface DynamicHandlerError {
    data object HandlerNotFound: DynamicHandlerError
    data object HostDoesNotExist: DynamicHandlerError
    data object ScenarioNotFound: DynamicHandlerError
    data class NoResponseForThisRequestInScenario(val scenarioName: String) : DynamicHandlerError
    data object NoResponseForThisRequest : DynamicHandlerError
    data class BadRequest(val exchangeKey: String) : DynamicHandlerError
}

typealias DynamicHandlerResult = Either<DynamicHandlerError, ProcessedRequest>

@Component
class DynamicHandlerServices(
    private val router: Router,
    private val problemsDomain: ProblemsDomain,
    private val transactionManager: TransactionManager,
    private val clock: Clock,
    private val handlebars: Handlebars
) {

    fun executeDynamicHandler(
        host: String,
        method: HttpMethod,
        path: String,
        request: HttpServletRequest,
        externalKey: String? = null,
    ) : DynamicHandlerResult {

        //val splitHost = host.split(".").first()

        if (!router.doesHostExist(host)) return failure(DynamicHandlerError.HostDoesNotExist)

        val dynamicHandler = router.match(host, method, path)
            ?: return failure(DynamicHandlerError.HandlerNotFound)

        if (!router.doesScenarioExist(dynamicHandler.routeNode, dynamicHandler.pathTemplate, method))
            return failure(DynamicHandlerError.ScenarioNotFound)

        val handlerResponse : HandlerResult = dynamicHandler.dynamicHandler.handle(request, handlebars)

        val exchangeKey = problemsDomain.generateUuidValue()
        val fails = handlerResponse.fails

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        transactionManager.run{
            val problemsRepository = it.problemsRepository

            val jsonRequestHeaders =
                if(handlerResponse.headers.isNotEmpty()) mapper.writeValueAsString(handlerResponse.headers)
                else null

            val date = clock.now().epochSeconds

            problemsRepository.addRequest(
                exchangeKey,
                dynamicHandler.pathTemplate,
                method.name,
                path,
                externalKey,
                host,
                jsonRequestHeaders,
                date
            )

            if(handlerResponse.body != null) {
                problemsRepository.addRequestBody(
                    exchangeKey,
                    handlerResponse.body.toByteArray(),
                    handlerResponse.headers["Content-Type"] ?: ""
                )
            }

            if(handlerResponse.params.isNotEmpty()) {
                problemsRepository.addRequestParams(exchangeKey, handlerResponse.params)
            }

            if(fails.isNotEmpty()) {
                problemsRepository.addProblems(exchangeKey, fails)
            } else {
                val jsonResponseHeaders =
                    if(handlerResponse.headers.isNotEmpty()) mapper.writeValueAsString(handlerResponse.headers)
                    else null

                val responseId = problemsRepository.addResponse(exchangeKey, handlerResponse.response!!.statusCode.code, jsonResponseHeaders)

                if(handlerResponse.processedBody != null) {
                    problemsRepository.addResponseBody(responseId, handlerResponse.processedBody, handlerResponse.response.contentType!!)
                }
            }
        }

        if (fails.isNotEmpty()) return failure(DynamicHandlerError.BadRequest(exchangeKey))

        return success(
            ProcessedRequest(
                exchangeKey = exchangeKey,
                statusCode = handlerResponse.response?.statusCode ?: StatusCode.OK,
                contentType = handlerResponse.response?.contentType,
                headers = handlerResponse.response?.headers ?: emptyMap(),
                body = handlerResponse.processedBody,
            )
        )
    }

    fun updateDynamicRouter() {

        val newMap = mutableMapOf<String, RouteNode>()

        val specs = transactionManager.run {
            val openAPIRepository = it.openAPIRepository
            openAPIRepository.uploadOpenAPI()
        }

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        for(sp in specs) {

            val paths = mutableListOf<ApiPath>()

            sp.spec.paths.forEach { apiPath ->

                val operations : List<PathOperation> = mapper.readValue(apiPath.operations, object : TypeReference<List<PathOperation>>() {})

                paths.add(
                    ApiPath(
                        fullPath = apiPath.path,
                        operations = operations,
                        path = splitPath(apiPath.path)
                    )
                )
            }

            val scenarios = sp.scenarios.map {
                Scenario(
                    name = it.name,
                    method = HttpMethod.valueOf(it.method.uppercase()),
                    path = it.path,
                    responses = it.responses.map { response ->
                        ResponseConfig(
                            statusCode = StatusCode.fromCode(response.statusCode)!!,
                            body = response.body,
                            headers = response.headers?.let { mapper.readValue(it, object : TypeReference<Map<String, String>>() {}) },
                            contentType = response.contentType
                        )
                    }
                )
            }

            val routeNode = router.createRouterNode(
                ApiSpec(
                    name = sp.spec.name,
                    description = sp.spec.description,
                    paths = paths
                ),
                scenarios
            )

            newMap[sp.host] = routeNode

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

    fun processTemplateBody(body: ByteArray?, context: Map<String, Any>): String? {
        if (body == null) return null

        val bodyString = body.decodeToString()

        val normalized = maybeUnescapeJsonString(bodyString)

        val handlebars = Handlebars()
        val template = handlebars.compileInline(normalized)
        return template.apply(context)
    }

    fun maybeUnescapeJsonString(input: String): String {
        val looksEscaped = input.contains("\\\"") || input.contains("\\u007b") || input.contains("\\\\")
        if (!looksEscaped) return input

        return input
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\u007b", "{")
            .replace("\\u007d", "}")
    }

}