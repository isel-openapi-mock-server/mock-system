package isel.openapi.mock.parsingServices.model

data class ApiResponse(
    val statusCode: StatusCode,
    val contentType: String?,
    val schemaType: Type
)