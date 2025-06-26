package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.http.DynamicHandler

class HandlerAndUUID(
    val dynamicHandler: DynamicHandler,
    val pathTemplate: String,
    val routeNode: RouteNode
)