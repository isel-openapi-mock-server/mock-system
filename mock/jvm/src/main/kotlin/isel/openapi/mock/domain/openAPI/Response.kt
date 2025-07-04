package isel.openapi.mock.domain.openAPI

data class Response(
    val statusCode: StatusCode,
    //val contentType: String?,
    val schema: ContentOrSchema.ContentField?,
    val headers: List<ApiHeader> = emptyList()
)