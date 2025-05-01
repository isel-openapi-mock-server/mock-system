package isel.openapi.admin.http.model

import isel.openapi.admin.parsingServices.model.HttpMethod
import isel.openapi.admin.parsingServices.model.StatusCode

data class Scenario(
    val name: String,
    val responses: List<ResponseConfig>
)

data class ResponseConfig(
    val method: HttpMethod,
    val path: String,
    val statusCode: StatusCode,
    val headers: Map<String, String>?,
    val body: ByteArray?,
)