package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.domain.openAPI.StatusCode

class ProcessedRequest(
    val exchangeKey : String,
    val statusCode: StatusCode,
    val contentType: String?,
    val headers: Map<String, String>?,
    val body: String?,
)