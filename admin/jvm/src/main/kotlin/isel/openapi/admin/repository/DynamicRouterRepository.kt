package isel.openapi.admin.repository

import isel.openapi.admin.domain.HostInfo
import isel.openapi.admin.domain.RouteNode
import org.springframework.stereotype.Component

@Component
class DynamicRoutesRepository{

    private val mappings = mutableMapOf<String, HostInfo>()

    fun register(host:String, root: RouteNode) {
        mappings[host] = HostInfo(root)
    }

    fun getOperations(host: String): HostInfo? {
        return mappings[host]
    }

}