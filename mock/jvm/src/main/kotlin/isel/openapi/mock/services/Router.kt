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

    fun register(apiSpec: ApiSpec, host: String) {

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
                    null
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
                        if(current.children.containsKey(part)) {
                            current.children[part]!!.operations.add(RouteOperation(operation.method, handler))
                            current
                        } else {
                            val newNode = RouteNode(part)
                            current.children[part] = newNode
                            newNode.operations.add(RouteOperation(operation.method, handler))
                            newNode
                        }

                    }
                }
            }
            repository.register(host, root)
        }
    }

    fun match(host: String, method: HttpMethod, path: String): HandlerAndUUID? {

        val op = repository.getOperations(host) ?: return null

        var current = op.root

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
            //scenarios = dynamicHandler?.scenarios ?: emptyList() //op.scenarios.filter { it.responseForPathMethod(path, method) }
            //isRootUpToDate = op.isRootUpToDate
        )
    }


    fun doesHostExist(host: String): Boolean = repository.getOperations(host) != null

    fun doesScenarioExist(host: String, scenarioName: String): Boolean =
        repository.getOperations(host)?.scenarios?.any { it.name == scenarioName } ?: false
}


