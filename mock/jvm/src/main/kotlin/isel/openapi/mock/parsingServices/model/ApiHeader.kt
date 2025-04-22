package isel.openapi.mock.parsingServices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ApiHeader( //@JsonCreator constructor(
    //@JsonProperty("name")
    val name: String,
    //@JsonProperty("description")
    val description: String?,
    //@JsonProperty("type")
    val type: ContentOrSchema,
    //@JsonProperty("required")
    val required: Boolean,
    //@JsonProperty("style")
    val style: ParameterStyle,
    //@JsonProperty("explode")
    val explode: Boolean,
)