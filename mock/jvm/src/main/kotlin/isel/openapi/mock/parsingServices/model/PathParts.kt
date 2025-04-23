package isel.openapi.mock.parsingServices.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PathParts.Static::class, name = "Static"),
    JsonSubTypes.Type(value = PathParts.Param::class, name = "Param")
)

sealed interface PathParts{
    data class Static(val name: String): PathParts
    data class Param(val name: String, val type: Type): PathParts
}

