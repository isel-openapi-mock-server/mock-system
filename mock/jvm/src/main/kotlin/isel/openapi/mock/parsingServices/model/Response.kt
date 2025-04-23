package isel.openapi.mock.parsingServices.model

import com.github.erosb.jsonsKema.JsonValue

data class Response(
    val statusCode: StatusCode,
    //val contentType: String?,
    val schema: ContentOrSchema?
)