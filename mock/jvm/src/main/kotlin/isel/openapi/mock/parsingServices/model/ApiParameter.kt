package isel.openapi.mock.parsingServices.model

data class ApiParameter(
    val name: String,
    val location: Location, // "query", "header", "path", "cookie"
    val description: String?,
    val type: Type,
    val required: Boolean,
    val allowEmptyValue: Boolean,// usado no parametro na query, "?param="
    val style: ParameterStyle, //
    val explode: Boolean,   // Se true: ?ids=1&ids=2&ids=3 é valido, se false: ?ids=1,2,3 é valido.
)