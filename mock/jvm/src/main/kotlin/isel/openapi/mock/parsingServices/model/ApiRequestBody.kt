package isel.openapi.mock.parsingServices.model

data class ApiRequestBody(
    val contentType: String,
    val schemaType: Type,
    val required: Boolean,
)