package isel.openapi.mock.http

import com.github.erosb.jsonsKema.*
import isel.openapi.mock.parsingServices.model.*
import kotlin.test.Test
import kotlin.test.assertTrue

class DynamicHandlersTests {

    private val response = listOf(Response(
        statusCode = StatusCode.OK,
        contentType = "application/json",
        schema = JsonParser(
            """
            {
                "type": "null"
            }
            """.trimIndent()
        ).parse()
    ))

    @Test
    fun objectVerificationTest() {

        val schemaJson: JsonValue = JsonParser(
            """
        {
            "type": "object",
            "properties": {
                "name": {
                    "type": "string"
                },
                "age": {
                    "type": "number",
                }
            }
        }
        
        """.trimIndent()).parse()

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schema = schemaJson,
                required = true
            )

        val body = """
            {
                "name": "John",
                "age": 30
            }
        """.trimIndent()
        val body2 = """
            {
                "name": "John",
                "age": "30"
            }
        """.trimIndent()
        val body3 = """
            bom dia
        """.trimIndent()

        val dynamicHandler = DynamicHandler(
            response = response,
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users"))),
            headers = null
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }
        assertTrue { result3[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }

    }

    @Test
    fun arrayVerificationTest() {

        val schemaJson: JsonValue = JsonParser(
            """
        {
            "type": "array",
            "items": {
                "type": "integer"
            }
        }
        
        """.trimIndent()).parse()

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schema = schemaJson,
                required = true
            )

        val body = """
            [1,2,3,4]
        """.trimIndent()
        val body2 = """
            [1,2,3,"4"]
        """.trimIndent()

        val dynamicHandler = DynamicHandler(
            response = response,
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users"))),
            headers = null
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }

    }

    @Test
    fun objectArrayVerificationTest() {

        val schemaJson: JsonValue = JsonParser(
            """
        {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "name": {
                        "type": "string"
                    },
                    "age": {
                        "type": "integer"
                    }
                }
            }
        }
        
        """.trimIndent()).parse()

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schema = schemaJson,
                required = true
            )

        val body = """
            [
                {
                    "name": "John",
                    "age": 30
                },
                {
                    "name": "Jane",
                    "age": 25
                }
            ]
        """.trimIndent()
        val body2 = """
            [
                {
                    "name": "John",
                    "age": 30
                },
                {
                    "name": "Jane",
                    "age": "25"
                }
            ]
        """.trimIndent()

        val dynamicHandler = DynamicHandler(
            response = response,
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users"))),
            headers = null
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }

    }

    @Test
    fun nullBodyVerificationTest() {

        val schemaJson: JsonValue = JsonParser(
            """
        {
            "type": "null"
        }
        
        """.trimIndent()).parse()

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schema = schemaJson,
                required = true
            )

        val body = ""

        val dynamicHandler = DynamicHandler(
            response = response,
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users"))),
            headers = null
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)

        assertTrue { result1[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }

    }

    @Test
    fun booleanBodyVerificationTest() {

        val schemaJson: JsonValue = JsonParser(
            """
        {
            "type": "boolean"
        }
        
        """.trimIndent()).parse()

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schema = schemaJson,
                required = true
            )

        val body = "true"
        val body2 = "false"
        val body3 = "null"

        val dynamicHandler = DynamicHandler(
            response = response,
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users"))),
            headers = null
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2.isEmpty() }
        assertTrue { result3[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }

    }

    @Test
    fun numberBodyVerificationTest() {

        val schemaJson: JsonValue = JsonParser(
            """
        {
            "type": "number"
        }
        
        """.trimIndent()).parse()

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schema = schemaJson,
                required = true
            )

        val body = "3.14"
        val body2 = "3"
        val body3 = "null"

        val dynamicHandler = DynamicHandler(
            response = response,
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users"))),
            headers = null
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2.isEmpty() }
        assertTrue { result3[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }

    }

    @Test
    fun stringBodyVerificationTest() {

        val schemaJson: JsonValue = JsonParser(
            """
        {
            "type": "string"
        }
        
        """.trimIndent()).parse()

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schema = schemaJson,
                required = true
            )

        val body = "\"hello\""
        val body2 = ""
        val body3 = "\"null\""

        val dynamicHandler = DynamicHandler(
            response = response,
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users"))),
            headers = null
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }
        assertTrue { result3.isEmpty() }

    }

    @Test
    fun integerBodyVerificationTest() {

        val schemaJson: JsonValue = JsonParser(
            """
        {
            "type": "integer"
        }
        
        """.trimIndent()).parse()

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schema = schemaJson,
                required = true
            )

        val body = "3"
        val body2 = "3.14"
        val body3 = "null"

        val dynamicHandler = DynamicHandler(
            response = response,
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users"))),
            headers = null
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }
        assertTrue { result3[0] == VerifyBodyError.InvalidBodyFormat(schemaJson.toString()) }

    }

    @Test
    fun headersVerificationTest() {

        val expectedHeaders = listOf(
            ApiHeader(
                name = "Content-Type",
                type = ContentOrSchema.SchemaObject(
                    schema = JsonParser(
                        """
                        {
                            "type": "string"
                        }
                        """.trimIndent()
                    ).parse()
                ),
                required = true,
                style = ParameterStyle.FORM,
                explode = false,
                description = "Content type of the request"
            ),
            ApiHeader(
                name = "Authorization",
                type = ContentOrSchema.SchemaObject(
                    schema = JsonParser(
                        """
                        {
                            "type": "string"
                        }
                        """.trimIndent()
                    ).parse()
                ),
                required = false,
                style = ParameterStyle.FORM,
                explode = false,
                description = "Authorization token"
            )
        )

        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "token123"
        )

        val headers1 = mapOf(
            "Content-Type" to "application/json",
        )
        val headers2 = mapOf(
            "Authorization" to "token123"
        )
        val headers3 = mapOf(
            "Content-Type" to "application/problem",
            "Authorization" to "token123"
        )
        val headers4 = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "token123",
            "Extra" to "extra"
        )

        val dynamicHandler = DynamicHandler(
            response = response,
            params = null,
            body = null,
            path = listOf((PathParts.Static("users"))),
            headers = expectedHeaders
        )

        val result1 = dynamicHandler.verifyHeaders(headers, expectedHeaders, "application/json")
        val result2 = dynamicHandler.verifyHeaders(headers1, expectedHeaders, "application/json")
        val result3 = dynamicHandler.verifyHeaders(headers2, expectedHeaders, "application/json")
        val result4 = dynamicHandler.verifyHeaders(headers3, expectedHeaders, "application/json")
        val result5 = dynamicHandler.verifyHeaders(headers4, expectedHeaders, "application/json")

        assertTrue { result1.isEmpty() }
        assertTrue { result2.isEmpty() }

        assertTrue { result3.size == 2 }
        assertTrue { result3[0] == VerifyHeadersError.MissingHeader("Content-Type") }
        assertTrue { result3[1] == VerifyHeadersError.MissingHeaderContent("Content-Type") }

        assertTrue { result4.size == 1 }
        assertTrue { result4[0] == VerifyHeadersError.InvalidContentType("application/json", "application/problem") }

        assertTrue { result5.isEmpty() }

    }

}