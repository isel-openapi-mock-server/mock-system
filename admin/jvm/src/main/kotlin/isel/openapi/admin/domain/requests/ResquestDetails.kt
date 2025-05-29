package isel.openapi.admin.domain.requests

data class RequestDetails(
    val externalKey: String,
    val url: String,
    val method: String,
    val host: String,
    val uuid: String,
)