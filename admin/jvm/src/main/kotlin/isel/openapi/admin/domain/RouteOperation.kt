package isel.openapi.admin.domain

import isel.openapi.admin.parsingServices.model.HttpMethod

data class RouteOperation(
    val method: HttpMethod,
    val handler: ResponseValidator
)