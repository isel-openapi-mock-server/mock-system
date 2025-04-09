package isel.openapi.mock.parsingServices.model

data class ApiServer(
    val url: String,
    val description: String?,
    val variables: List<ServerVariable> //https://api.example.com/{username} -> List(ServerVariable(username,...))
)
