package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.parsingServices.model.Response

class ResponseInfo(
    val response: Response,
    val body: String? = null,
    val headers: Map<String, String> = emptyMap(),
)