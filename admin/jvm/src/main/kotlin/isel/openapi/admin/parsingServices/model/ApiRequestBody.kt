package isel.openapi.admin.parsingServices.model


data class ApiRequestBody(
    val content: ContentOrSchema.ContentField,
    val required: Boolean,
)