package isel.openapi.mock.domain.openAPI

data class ApiPath(
    val fullPath: String,
    val path: List<PathParts>,
    val operations: List<PathOperation>
)