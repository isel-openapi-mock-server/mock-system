package isel.openapi.mock.domain.openAPI


data class ApiRequestBody(
    val content: ContentOrSchema.ContentField,
    val required: Boolean,
)