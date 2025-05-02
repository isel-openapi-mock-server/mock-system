package isel.openapi.mock.services

import isel.openapi.mock.domain.dynamic.HostInfo
import org.springframework.stereotype.Component

data class RouteVersion(
    val id: String, //Identificador da transação em que foi realziada a alteração???
    val routes: Map<String, HostInfo>,
    val previous: RouteVersion? = null,
)

@Component
class Graph {

    private var currentVersion : RouteVersion? = null

    fun updateCurrentVersion(id: String, host: String, root: HostInfo) {
        val newVersion = RouteVersion(id, mapOf(host to root), currentVersion)
        currentVersion = newVersion
    }

    //Se em cada nó apenas existir os hosts que foram alterados
    fun getCurrent(host: String): HostInfo? {
        var current = currentVersion
        while(current != null) {
            val hostInfo = current.routes[host]
            if (hostInfo != null) {
                return hostInfo
            }
            current = current.previous
        }
        return null
    }
    //Se em cada nó existir todos os hosts
    /*
    fun getCurrent(host: String): HostInfo? {
        return currentVersion?.routes?.get(host)
    }
     */

}
