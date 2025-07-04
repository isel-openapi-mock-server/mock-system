package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.domain.problems.ParameterInfo
import isel.openapi.mock.http.VerificationError
import isel.openapi.mock.services.ResponseConfig
import jakarta.servlet.http.Cookie

class HandlerResult(
    val fails: List<VerificationError>,
    val body: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val cookies: List<Cookie> = emptyList(),
    val params: List<ParameterInfo>,
    val response: ResponseConfig?,
    val processedBody: ByteArray? = null,
)