package isel.openapi.admin.parsingServices.model

data class Response(
    val statusCode: StatusCode,
    //val contentType: String?,
    val schema: ContentOrSchema?
    //val headers: , TODO() faltam os headers
)