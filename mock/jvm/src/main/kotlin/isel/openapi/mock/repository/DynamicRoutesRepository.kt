package isel.openapi.mock.repository

import isel.openapi.mock.domain.dynamic.HostInfo
import isel.openapi.mock.domain.dynamic.RouteNode
import org.springframework.stereotype.Component

@Component
class DynamicRoutesRepository{

    private var mappings: Map<String, HostInfo> = emptyMap()

    fun register(map: Map<String, RouteNode>) {
        mappings =
    }

    fun getOperations(host: String): HostInfo? {
        return mappings[host]
    }

}