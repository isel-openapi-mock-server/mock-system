package isel.openapi.admin.domain.requests

data class RequestInfo(
    val exchangeKey: String,
    val externalKey: String?,
    val method: String,
    val path: String,
    val host: String,
    val body: ByteArray?,
    val problems: List<ProblemInfo>,
    val response: ResponseInfo? = null,
)