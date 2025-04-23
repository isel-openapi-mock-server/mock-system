package isel.openapi.admin.parsingServices.model

data class ApiResponse(
    val statusCode: StatusCode,
    val contentType: String?,
    val schemaType: Type
)