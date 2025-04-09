package isel.openapi.mock.parsingServices.model

sealed interface PathParts{
    data class Static(val name: String): PathParts
    data class Param(val name: String, val type: Type): PathParts
}