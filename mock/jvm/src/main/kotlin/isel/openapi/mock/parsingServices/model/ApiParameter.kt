package isel.openapi.mock.parsingServices.model

import com.github.erosb.jsonsKema.JsonValue

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
sealed interface ContentOrSchema {

    data class SchemaObject(val schema: JsonValue?) : ContentOrSchema

    //Se for um parametro ou um header o map tem apenas uma chave
    data class ContentField(val content: Map<String, SchemaObject>) : ContentOrSchema

}

//Para possivelmente adicionarmos o enconding
//data class MediaTypeObject(val schema: JsonValue?)
