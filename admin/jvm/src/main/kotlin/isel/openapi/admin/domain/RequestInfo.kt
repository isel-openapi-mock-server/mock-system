package isel.openapi.admin.domain

data class RequestInfo(
    val uuid: String,
    val externalKey: String,
    val method: String,
    val path: String,
    val host: String,
    val body: ByteArray?,
    val headers: List<HeadersInfo>,
    val problems: List<ProblemInfo>,
)