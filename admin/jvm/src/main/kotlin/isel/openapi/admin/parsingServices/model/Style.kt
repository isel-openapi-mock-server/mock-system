package isel.openapi.admin.parsingServices.model

data class Style(
    val style: ParameterStyle,
    val explode: Boolean,
    val location: Location,
    val type: Type
)