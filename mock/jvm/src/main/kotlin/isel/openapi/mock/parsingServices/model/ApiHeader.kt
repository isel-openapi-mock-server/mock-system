package isel.openapi.mock.parsingServices.model

data class ApiHeader(
    val name: String,
    val description: String?,
    val type: Type,
    val required: Boolean,
    val style: ParameterStyle,
    val explode: Boolean,
)