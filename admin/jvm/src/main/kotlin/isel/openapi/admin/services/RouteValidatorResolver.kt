package isel.openapi.admin.services

import isel.openapi.admin.domain.admin.AdminDomain
import isel.openapi.admin.domain.admin.ResponseValidator
import isel.openapi.admin.domain.admin.RouteNode
import isel.openapi.admin.domain.admin.RouteValidator
import isel.openapi.admin.parsing.model.ApiSpec
import isel.openapi.admin.parsing.model.HttpMethod
import isel.openapi.admin.repository.ResolverRepository
import org.springframework.stereotype.Component

@Component
class RouteValidatorResolver(
    private val repository: ResolverRepository,
    private val dynamicDomain: AdminDomain,
) {

    fun register(apiSpec: ApiSpec, transactionToken: String) {

        val root = RouteNode("")

        apiSpec.paths.forEach { apiPath ->
            apiPath.operations.forEach { operation ->

                val responseValidator = ResponseValidator(
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
                            current.children[part]!!.validators.add(RouteValidator(operation.method, responseValidator))
                            current
                        } else {
                            val newNode = RouteNode(part)
                            current.children[part] = newNode
                            newNode.validators.add(RouteValidator(operation.method, responseValidator))
                            newNode
                        }

                    }
                }
            }
            repository.register(transactionToken, root)
        }
    }

    fun match(transactionToken: String, method: HttpMethod, path: String): ResponseValidator? {

        var current = repository.getValidator(transactionToken) ?: return null

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

        return current.validators.find { it.method == method }?.validator
    }

    fun remove(transactionToken: String) {
        repository.remove(transactionToken)
    }

}


