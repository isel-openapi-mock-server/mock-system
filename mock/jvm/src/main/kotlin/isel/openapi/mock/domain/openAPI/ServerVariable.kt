package isel.openapi.mock.domain.openAPI

data class ServerVariable(
    val name: String, //https://api.example.com/{username} -> username
    val defaultValue: String, //valor default da variavel
    val enum: List<String>, //valores possiveis da variavel
)