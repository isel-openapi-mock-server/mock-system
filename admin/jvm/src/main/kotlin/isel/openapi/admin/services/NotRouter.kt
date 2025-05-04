package isel.openapi.admin.services

import isel.openapi.admin.domain.*
import isel.openapi.admin.parsingServices.model.ApiSpec
import isel.openapi.admin.parsingServices.model.HttpMethod
import isel.openapi.admin.repository.DynamicRoutesRepository
import org.springframework.stereotype.Component

@Component
class NotRouter(
    private val repository: DynamicRoutesRepository,
    private val dynamicDomain: AdminDomain,
) {

    fun register(apiSpec: ApiSpec, host: String) {

        val root = RouteNode("")

        apiSpec.paths.forEach { apiPath ->
            apiPath.operations.forEach { operation ->

                val handler = ResponseValidator( // TODO() mudar nome do val
                    operation.responses,
                    dynamicDomain
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

    fun match(host: String, method: HttpMethod, path: String): ResponseValidator? {

        val op = repository.getOperations(host) ?: return null

        var current = op.root

        var resourceUrl = path.split("?").first()

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
                    resourceUrl = resourceUrl.replace(part, current.part)
                } else {
                    return null
                }
            }
        }

        return current.operations.find { it.method == method }?.handler
    }


    fun doesHostExist(host: String): Boolean = repository.getOperations(host) != null



    //fun doesScenarioExist(host: String, scenarioName: String): Boolean =
    //    repository.getOperations(host)?.scenarios?.any { it.name == scenarioName } ?: false
}


