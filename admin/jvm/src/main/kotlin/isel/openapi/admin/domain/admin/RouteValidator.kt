package isel.openapi.admin.domain.admin

import isel.openapi.admin.parsing.model.HttpMethod

data class RouteValidator(
    val method: HttpMethod,
    val validator: ResponseValidator
)