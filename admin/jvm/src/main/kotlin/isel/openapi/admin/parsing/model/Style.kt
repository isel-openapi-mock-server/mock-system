package isel.openapi.admin.parsing.model

data class Style(
    val style: ParameterStyle,
    val explode: Boolean,
    val location: Location,
    val type: Type
)