package isel.openapi.mock.parsingServices.model

import io.swagger.v3.oas.models.security.SecurityRequirement

data class PathOperation(
    val method: HttpMethod,
    val security: Boolean,
    val parameters: List<ApiParameter>,
    val requestBody: ApiRequestBody?,
    val responses: List<Response>,
    val servers: List<String>,
    val headers: List<ApiHeader>,
)