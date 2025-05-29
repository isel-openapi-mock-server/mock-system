package isel.openapi.admin.parsing.model


data class ApiRequestBody(
    val content: ContentOrSchema.ContentField,
    val required: Boolean,
)