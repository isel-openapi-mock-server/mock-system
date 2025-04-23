package isel.openapi.mock

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.erosb.jsonsKema.JsonParser
import com.google.gson.Gson
import isel.openapi.mock.http.DynamicHandler
import isel.openapi.mock.http.DynamicHandlersTests.Companion.dynamicDomain
import isel.openapi.mock.parsingServices.Parsing
import isel.openapi.mock.parsingServices.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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


    @Test
    fun b() {
        val parsing = Parsing()

        val isValid = parsing.validateOpenApi(openAPIDefinition)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val paths = parsing.extractApiSpec(definition).paths

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()
        //.setSerializationInclusion(JsonInclude.Include.ALWAYS)
        /*.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
            JsonTypeInfo.As.PROPERTY
        )*/

        val json = mapper.writeValueAsString(paths)

        println("serialized:$json")

        val responseFromJson: List<ApiPath> = mapper.readValue(json, object : TypeReference<List<ApiPath>>() {})

        println("deserialized:$responseFromJson")
    }

    private val openAPIDefinition = """
openapi: 3.0.4
info:
  title: "API"
  version: "1.0"
servers:
  - url: "https://api.exemplo.com/"
paths:
  /api:
    get:
      summary: "Obtém informações gerais"
      responses:
        '200':
          description: "OK"
    post:
      summary: "Cria um novo recurso"
      responses:
        '201':
          description: "Created"
  /api/{id}:
    get:
      summary: "Obtém um recurso específico"
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
        - name: num
          in: header
          schema:
            type: array
            items:
              type: integer
      responses:
        '200':
          description: "OK"
    put:
      summary: "Atualiza um recurso específico"
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: "OK"
    delete:
      summary: "Remove um recurso específico"
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      responses:
        '204':
          description: "No Content"
        """.trimIndent()
}