package isel.openapi.mock.repository

import isel.openapi.mock.services.RouteNode
import isel.openapi.mock.services.Router
import isel.openapi.mock.parsingServices.model.ApiSpec
import org.springframework.stereotype.Component

@Component
class DynamicRoutesRepository{

    private val mappings = mutableMapOf<String, RouteNode>()

    fun register(host:String, root: RouteNode) {
        if(!mappings.containsKey(host)) {
            mappings[host] = root
        }
    }

    fun getOperations(host: String): RouteNode? {
        return mappings[host]
    }

}