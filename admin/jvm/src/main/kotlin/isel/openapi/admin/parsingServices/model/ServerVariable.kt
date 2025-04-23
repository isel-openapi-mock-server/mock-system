package isel.openapi.admin.parsingServices.model

data class ServerVariable(
    val name: String, //https://api.example.com/{username} -> username
    val defaultValue: String, //valor default da variavel
    val enum: List<String>, //valores possiveis da variavel
)