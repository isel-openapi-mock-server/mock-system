package isel.openapi.admin.http.model

data class Scenario(
    val name: String,
    val path: String,
    val method: String,
    val responses: List<ResponseConfig>
)

data class ResponseConfig(
    val statusCode: String,
    val contentType: String?,
    val headers: Map<String, String>?,
    val body: String?,
)