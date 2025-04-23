package isel.openapi.admin.domain

data class RequestDetails(
    val externalKey: String,
    val url: String,
    val method: String,
    val host: String,
    val uuid: String,
)