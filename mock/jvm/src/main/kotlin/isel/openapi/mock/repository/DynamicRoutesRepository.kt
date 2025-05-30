package isel.openapi.mock.repository

import isel.openapi.mock.domain.dynamic.RouteNode
import isel.openapi.mock.domain.openAPI.HttpMethod
import org.springframework.stereotype.Component

@Component
class DynamicRoutesRepository{

    private var mappings: Map<String, RouteNode> = emptyMap()

    fun register(map: Map<String, RouteNode>) {
        mappings = map
    }

    fun getOperations(host: String): RouteNode? {
        return mappings[host]
    }

    fun isScenarioExists(
        routeNode: RouteNode,
        scenarioName: String,
        path: String,
        method: HttpMethod
    ): Boolean {
        return  routeNode.operations.find {
            it.method == method && it.fullPath== path && it.scenariosNames.contains(scenarioName)
        } != null
    }

}