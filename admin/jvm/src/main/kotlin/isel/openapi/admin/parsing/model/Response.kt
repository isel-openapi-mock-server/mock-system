package isel.openapi.admin.parsing.model

data class Response(
    val statusCode: StatusCode,
    //val contentType: String?,
    val schema: ContentOrSchema.ContentField?,
    val headers: List<ApiHeader> = emptyList()
)