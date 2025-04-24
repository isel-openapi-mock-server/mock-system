package isel.openapi.mock.domain.openAPI

data class PathOperation(
    val method: HttpMethod,
    val security: Boolean,
    val parameters: List<ApiParameter>,
    val requestBody: ApiRequestBody?,
    val responses: List<Response>,
    val servers: List<String>,
    val headers: List<ApiHeader>,
)