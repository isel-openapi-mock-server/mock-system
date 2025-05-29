package isel.openapi.admin.parsing.model

data class ApiSpec(
    val name: String,
    val description: String?,
    //val servers: List<ApiServer>,
    val paths: List<ApiPath>,
    //val components : Map<String, Any>,
)