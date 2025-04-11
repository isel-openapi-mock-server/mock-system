package isel.openapi.mock.parsingServices.model


data class ApiRequestBody(
    val content: ContentOrSchema.ContentField,
    val required: Boolean,
)