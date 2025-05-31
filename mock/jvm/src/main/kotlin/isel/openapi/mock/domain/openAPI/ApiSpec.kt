package isel.openapi.mock.domain.openAPI

data class ApiSpec(
    val name: String,
    val description: String?,
    val paths: List<ApiPath>,
)