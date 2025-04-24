package isel.openapi.mock.domain.dynamic

class RouteNode(val part: String) {
    val children = mutableMapOf<String, RouteNode>()
    val isParameter = part.startsWith("{")
    var operations = mutableSetOf<RouteOperation>()
}