package isel.openapi.admin.parsingServices.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class PathParts(val name: String, val isParam: Boolean)