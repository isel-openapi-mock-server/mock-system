package isel.openapi.mock.domain.problems

import isel.openapi.mock.domain.openAPI.ContentOrSchema
import isel.openapi.mock.domain.openAPI.Location

class ParameterInfo(
    val name: String,
    val content: String,
    val location: Location,
    val type: ContentOrSchema,
) {
    fun typeToString(): String {
        return when (type) {
            is ContentOrSchema.ContentField -> type.content.toString()
            is ContentOrSchema.SchemaObject -> type.toString()
        }
    }
}