package isel.openapi.mock.services

import isel.openapi.mock.domain.dynamic.DynamicDomain
import isel.openapi.mock.domain.dynamic.HandlerAndUUID
import isel.openapi.mock.domain.dynamic.RouteNode
import isel.openapi.mock.domain.dynamic.RouteOperation
import isel.openapi.mock.http.DynamicHandler
import isel.openapi.mock.domain.openAPI.ApiSpec
import isel.openapi.mock.domain.openAPI.HttpMethod
import isel.openapi.mock.repository.DynamicRoutesRepository
import org.springframework.stereotype.Component

@Component
class Router(
    private val repository: DynamicRoutesRepository,
    private val dynamicDomain: DynamicDomain,
) {

    fun createRouterNode(apiSpec: ApiSpec, scenarios: List<Scenario>) : RouteNode {

        val root = RouteNode("")

        apiSpec.paths.forEach { apiPath ->
            apiPath.operations.forEach { operation ->

                val handler = DynamicHandler(
                    apiPath.path,
                    operation.method,
                    operation.parameters,
                    operation.requestBody,
                    operation.headers,
                    operation.security,
                    dynamicDomain,
                    scenarios.firstOrNull { it.method == operation.method && it.path == apiPath.fullPath }
                )

                val parts = apiPath.fullPath.split("/").filter { it.isNotEmpty() }
                var current = root

                for (i in 0..parts.lastIndex) {
                    val part = parts[i]
                    current = if (i != parts.lastIndex) {
                        if(!current.children.containsKey(part)) {
                            val newNode = RouteNode(part)
                            current.children[part] = newNode
                            newNode
                        } else {
                            current.children[part]!!
                        }
                    } else {
                        //TODO: TESTE FALHA AQUI? TIMEOUT
                        if(current.children.containsKey(part)) {
                            current.children[part]!!.operations.add(
                                RouteOperation(
                                    operation.method,
                                    apiPath.fullPath,
                                    handler))
                            current
                        } else {
                            val newNode = RouteNode(part)
                            current.children[part] = newNode
                            newNode.operations.add(
                                RouteOperation(
                                    operation.method,
                                    apiPath.fullPath,
                                    handler))
                            newNode
                        }
                    }
                }
            }
        }
        return root
    }

    fun register(map: Map<String, RouteNode>) {
        repository.register(map)
    }

    fun match(host: String, method: HttpMethod, path: String): HandlerAndUUID? {

        var current = repository.getOperations(host) ?: return null

        val resourceUrl = path.split("?").first().split("/").toMutableList()

        val parts = path.split("/").filter { it.isNotEmpty() }
        val params = mutableMapOf<String, String>()

        for (i in parts.indices) {
            if (current.children.containsKey(parts[i])) {
                current = current.children[parts[i]]!!
            } else {
                val wildcardChild = current.children.values.find { it.isParameter }
                if (wildcardChild != null) {
                    current = wildcardChild
                    params[wildcardChild.part.removePrefix("{").removeSuffix("}")] = parts[i]
                    resourceUrl[i+1] = current.part
                } else {
                    return null
                }
            }
        }

        val dynamicHandler = current.operations.find { it.method == method }?.handler ?: return null

        return HandlerAndUUID(
            dynamicHandler = dynamicHandler,
            resourceUrl = resourceUrl.joinToString("/"),
            routeNode = current
        )
    }


    fun doesHostExist(host: String): Boolean = repository.getOperations(host) != null

    fun doesScenarioExist(routeNode: RouteNode, path: String, method: HttpMethod): Boolean =
        repository.isScenarioExists(routeNode, path, method)
}


