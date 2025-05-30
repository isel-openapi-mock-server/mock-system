package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.domain.openAPI.Response

class ResponseInfo(
    val statusCode: String,
    val contentType: String?,
    val headers: String?,
    val body: ByteArray?,
)