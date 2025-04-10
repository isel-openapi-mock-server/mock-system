package isel.openapi.mock.services

import isel.openapi.mock.http.DynamicHandler
import isel.openapi.mock.parsingServices.model.ApiSpec
import isel.openapi.mock.parsingServices.model.HttpMethod
import isel.openapi.mock.repository.DynamicRoutesRepository
import org.springframework.stereotype.Component

class RouteOperation(
    val method: HttpMethod,
    val handler: DynamicHandler
)

class RouteNode(val part: String) {
    val children = mutableMapOf<String, RouteNode>()
    val isParameter = part.startsWith("{")
    var operations = mutableSetOf<RouteOperation>()
}

@Component
class Router(
    private val repository: DynamicRoutesRepository
    //TODO: domain
) {

    fun register(apiSpec: ApiSpec) {

        val host = "mock" //TODO: get host
        val root = RouteNode("")

        apiSpec.paths.forEach { apiPath ->
            apiPath.operations.forEach { operation ->

                val handler = DynamicHandler(
                    apiPath.path,
                    operation.responses,
                    operation.parameters,
                    operation.requestBody,
                    operation.headers
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

    fun match(host: String, method: HttpMethod, path: String): DynamicHandler? {

        var current = repository.getOperations(host) ?: return null

        val parts = path.split("/").filter { it.isNotEmpty() }
        val params = mutableMapOf<String, String>()

        for (part in parts) {
            if (current.children.containsKey(part)) {
                current = current.children[part]!!
            } else {
                val wildcardChild = current.children.values.find { it.isParameter }
                if (wildcardChild != null) {
                    current = wildcardChild
                    params[wildcardChild.part.removePrefix("{").removeSuffix("}")] = part
                } else {
                    return null
                }
            }
        }
        return current.operations
            .firstOrNull { it.method == method }
            ?.handler
    }
}


