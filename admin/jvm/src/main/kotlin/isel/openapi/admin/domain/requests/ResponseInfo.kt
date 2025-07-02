package isel.openapi.admin.domain.requests

class ResponseInfo(
    val body : ByteArray?,
    val statusCode: Int,
    val headers: Map<String, String>,
    val contentType : String?
)