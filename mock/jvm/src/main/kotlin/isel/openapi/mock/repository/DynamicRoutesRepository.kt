package isel.openapi.mock.repository

import isel.openapi.mock.domain.dynamic.HostInfo
import isel.openapi.mock.domain.dynamic.RouteNode
import org.springframework.stereotype.Component

@Component
class DynamicRoutesRepository{

    private val mappings = mutableMapOf<String, HostInfo>()

    fun register(host:String, root: RouteNode) {
        mappings[host] = HostInfo(root, true)
    }

    fun getOperations(host: String): HostInfo? {
        return mappings[host]
    }

}