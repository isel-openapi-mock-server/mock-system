package isel.openapi.admin.parsingServices.model

data class ApiSpec(
    val name: String,
    val description: String?,
    //val servers: List<ApiServer>,
    val paths: List<ApiPath>,
    //val components : Map<String, Any>,
)