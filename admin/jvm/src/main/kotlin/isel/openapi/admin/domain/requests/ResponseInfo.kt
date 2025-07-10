package isel.openapi.admin.domain.requests

class ResponseInfo(
    val body : ByteArray?,
    val statusCode: Int,
    val contentType : String?
)