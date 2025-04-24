package isel.openapi.mock.domain.openAPI

data class ApiHeader(
    val name: String,
    val description: String?,
    val type: ContentOrSchema,
    val required: Boolean,
    val style: ParameterStyle,
    val explode: Boolean,
)