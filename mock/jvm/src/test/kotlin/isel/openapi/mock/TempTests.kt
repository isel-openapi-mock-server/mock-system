package isel.openapi.mock

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.erosb.jsonsKema.JsonParser
import com.google.gson.Gson
import isel.openapi.mock.http.DynamicHandler
import isel.openapi.mock.http.DynamicHandlersTests.Companion.dynamicDomain
import isel.openapi.mock.parsingServices.model.*
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

        val expectedHeaders = listOf(
            ApiHeader(
                name = "Content-Type",
                type = ContentOrSchema.SchemaObject(
                    schema = null
                ),
                required = true,
                style = ParameterStyle.FORM,
                explode = false,
                description = "Content type of the request"
            )
        )

        val dynamicHandler = DynamicHandler(
            responses = response,
            params = null,
            body = null,
            path = listOf((PathParts.Static("users"))),
            headers = expectedHeaders,
            dynamicDomain = dynamicDomain
        )

        //val gson = Gson()

        //val json = gson.toJson(dynamicHandler)

        val mapper = jacksonObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val json = mapper.writeValueAsString(response)

        println(json)

        val responseFromJson: List<Response> = mapper.readValue(json, object : TypeReference<List<Response>>() {})

        println(responseFromJson)
    }
}