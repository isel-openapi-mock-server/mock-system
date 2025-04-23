package isel.openapi.mock.parsingServices.model

data class ApiPath(
    val fullPath: String,
    val path: List<PathParts>,
    val operations: List<PathOperation>
)