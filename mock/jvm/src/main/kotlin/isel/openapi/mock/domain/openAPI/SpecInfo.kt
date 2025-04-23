package isel.openapi.mock.domain.openAPI

data class SpecInfo(
    val name: String,
    val description: String?,
    val paths: List<PathOperations>,
)