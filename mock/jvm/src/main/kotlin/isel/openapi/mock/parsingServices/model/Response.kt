package isel.openapi.mock.parsingServices.model

data class Response(
    val statusCode: StatusCode,
    val contentType: String?,
    val schemaType: Type
)