package isel.openapi.admin.parsing.model

data class ApiPath(
    val fullPath: String,
    val path: List<PathParts>,
    val operations: List<PathOperation>
)