package isel.openapi.mock.domain.dynamic

class ResponseInfo(
    val statusCode: String,
    val contentType: String?,
    val headers: String?,
    val body: ByteArray?,
)