package isel.openapi.admin.domain.requests

data class RequestDetails(
    val externalKey: String?,
    val pathTemplate: String,
    val method: String,
    val host: String,
    val uuid: String,
)