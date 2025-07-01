package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.domain.openAPI.HttpMethod
import isel.openapi.mock.http.DynamicHandler

class RouteOperation(
    val method: HttpMethod,
    val fullPath: String,
    val handler: DynamicHandler
)