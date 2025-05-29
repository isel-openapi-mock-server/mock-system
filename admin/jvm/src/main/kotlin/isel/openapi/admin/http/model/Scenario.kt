package isel.openapi.admin.http.model

import isel.openapi.admin.parsing.model.HttpMethod
import isel.openapi.admin.parsing.model.StatusCode

data class Scenario(
    val name: String,
    val path: String,
    val method: HttpMethod,
    val responses: List<ResponseConfig>
)

data class ResponseConfig(
    val statusCode: StatusCode,
    val contentType: String?,
    val headers: Map<String, String>?,
    val body: ByteArray?,
)