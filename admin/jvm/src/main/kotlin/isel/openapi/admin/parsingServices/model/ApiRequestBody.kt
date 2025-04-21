package isel.openapi.admin.parsingServices.model

data class ApiRequestBody(
    val contentType: String,
    val schemaType: Type,
    val required: Boolean,
)