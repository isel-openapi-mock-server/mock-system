package isel.openapi.mock.domain.openAPI

data class ApiServer(
    val url: String,
    val description: String?,
    val variables: List<ServerVariable> //https://api.example.com/{username} -> List(ServerVariable(username,...))
)
