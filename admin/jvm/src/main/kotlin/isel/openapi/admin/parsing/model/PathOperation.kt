package isel.openapi.admin.parsing.model

data class PathOperation(
    val method: HttpMethod,
    val security: Boolean,
    val parameters: List<ApiParameter>,
    val requestBody: ApiRequestBody?,
    val responses: List<Response>,
    val servers: List<String>,
    val headers: List<ApiHeader>,
)