package isel.openapi.admin.repository

import isel.openapi.admin.domain.admin.RouteNode
import org.springframework.stereotype.Component

@Component
class ResolverRepository{

    private val mappings = mutableMapOf<String, RouteNode>()

    fun register(transactionToken:String, root: RouteNode) {
        mappings[transactionToken] = root
    }

    fun getValidator(host: String): RouteNode? {
        return mappings[host]
    }

    fun remove(transactionToken: String) {
        mappings.remove(transactionToken)
    }

}