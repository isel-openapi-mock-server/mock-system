package isel.openapi.admin.domain.admin

class RouteNode(val part: String) {
    val children = mutableMapOf<String, RouteNode>()
    val isParameter = part.startsWith("{")
    var validators = mutableSetOf<RouteValidator>()
}