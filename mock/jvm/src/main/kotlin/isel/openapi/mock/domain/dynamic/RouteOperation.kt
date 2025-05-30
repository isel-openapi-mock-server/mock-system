package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.domain.openAPI.HttpMethod
import isel.openapi.mock.http.DynamicHandler

class RouteOperation(
    val method: HttpMethod,
    val fullPath: String,
    val scenariosNames : List<String>,
    val handler: DynamicHandler
)