package isel.openapi.admin.parsingServices.model

import io.swagger.v3.oas.models.security.SecurityRequirement

data class PathOperation(
    val method: HttpMethod,
    val security: List<SecurityRequirement>,
    val parameters: List<ApiParameter>,
    val requestBody: ApiRequestBody?,
    val responses: List<ApiResponse>,
    val servers: List<String>,
)