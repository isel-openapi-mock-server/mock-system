package isel.openapi.admin

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import isel.openapi.admin.parsingServices.model.*
import kotlin.test.Test

class TempTests {

    @Test
    fun asd() {

        val response = listOf(
            Response(
            statusCode = StatusCode.OK,
            schema = ContentOrSchema.ContentField(
                content = mapOf(
                    Pair(
                        "application/json",
                        ContentOrSchema.SchemaObject(
                            """
                            {
                                "type": "null"
                            }
                            """.trimIndent()
                        )
                    )
                )
            )
            )
        )

        val expectedHeaders =
            ApiHeader(
                name = "Content-Type",
                type = ContentOrSchema.SchemaObject(
                    schema = null
                ),
                required = true,
                style = ParameterStyle.FORM,
                explode = false,
                description = null
            )


        //val gson = Gson()

        //val json = gson.toJson(dynamicHandler)

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()
            //.setSerializationInclusion(JsonInclude.Include.ALWAYS)
            /*.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
                JsonTypeInfo.As.PROPERTY
            )*/

        val json = mapper.writeValueAsString(expectedHeaders)

        println(json)

        val responseFromJson: ApiHeader = mapper.readValue(json, object : TypeReference<ApiHeader>() {})

        println("first deserialization: $responseFromJson")
        println("type value: ${responseFromJson.type}")
        val a = ContentOrSchema.SchemaObject(null)
        val aJson = mapper.writeValueAsString(a)
        val aFromJson: ContentOrSchema.SchemaObject = mapper.readValue(aJson, object : TypeReference<ContentOrSchema.SchemaObject>() {})
        println("correct type value: $expectedHeaders")
        if (responseFromJson.type is ContentOrSchema.SchemaObject) {
            println("schema")
        }
        if (responseFromJson.type is ContentOrSchema.ContentField)
            println("content")
    }
}