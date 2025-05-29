package isel.openapi.admin.domain.admin

data class SpecInfo(
    val name: String,
    val description: String?,
    val paths: List<PathOperations>,
)