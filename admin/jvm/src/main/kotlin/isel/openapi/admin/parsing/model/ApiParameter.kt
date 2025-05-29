package isel.openapi.admin.parsing.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class ApiParameter(
    val name: String,
    val location: Location, // "query", "header", "path", "cookie"
    val description: String?,
    val type: ContentOrSchema,
    val required: Boolean,
    val allowEmptyValue: Boolean,// usado no parametro na query, "?param="
    val style: ParameterStyle, //
    val explode: Boolean,   // Se true: ?ids=1&ids=2&ids=3 é valido, se false: ?ids=1,2,3 é valido.
)

//TODO meter noutro sitio
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ContentOrSchema.SchemaObject::class, name = "SchemaObject"),
    JsonSubTypes.Type(value = ContentOrSchema.ContentField::class, name = "ContentField")
)
sealed interface ContentOrSchema {

    data class SchemaObject(val schema: String?) : ContentOrSchema {
        override fun toString(): String {
            return schema.toString()
        }
    }

    //Se for um parametro ou um header o map tem apenas uma chave
    data class ContentField(val content: Map<String, SchemaObject>) : ContentOrSchema {
        override fun toString(): String {
            return content.toString()
        }
    }

}
