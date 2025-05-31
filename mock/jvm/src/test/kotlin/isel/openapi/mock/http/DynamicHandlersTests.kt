package isel.openapi.mock.http

import isel.openapi.mock.domain.dynamic.DynamicDomain
import isel.openapi.mock.domain.openAPI.*
import jakarta.servlet.http.Cookie
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DynamicHandlersTests {

    @Test
    fun objectVerificationTest() {

        val schemaJson =
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
        
        """.trimIndent()

        val contentType = "application/json"

        val expectedBody =
            ApiRequestBody(
                content = ContentOrSchema.ContentField(mapOf(
                    contentType to ContentOrSchema.SchemaObject(
                        schema = schemaJson
                    )
                )),
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
            params = null,
            body = expectedBody,
            path = listOf((PathParts("users", false))),
            headers = null,
            security = false,
            dynamicDomain = dynamicDomain,
            scenarios = emptyList(),
            method = HttpMethod.POST,
        )

        val result1 = dynamicDomain.verifyBody(contentType, body, expectedBody)
        val result2 = dynamicDomain.verifyBody(contentType, body2, expectedBody)
        val result3 = dynamicDomain.verifyBody(contentType, body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson.toString(),
            receivedBody = body2,
        ), result2[0])
        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson.toString(),
            receivedBody = body3,
        ), result3[0])

    }

    @Test
    fun arrayVerificationTest() {

        val schemaJson =
            """
        {
            "type": "array",
            "items": {
                "type": "integer"
            }
        }
        
        """.trimIndent()

        val contentType = "application/json"

        val expectedBody =
            ApiRequestBody(
                content = ContentOrSchema.ContentField(mapOf(
                    contentType to ContentOrSchema.SchemaObject(
                        schema = schemaJson
                    )
                )),
                required = true
            )

        val body = """
            [1,2,3,4]
        """.trimIndent()
        val body2 = """
            [1,2,3,"4"]
        """.trimIndent()

        val dynamicHandler = DynamicHandler(
            params = null,
            body = expectedBody,
            path = listOf((PathParts("users", false))),
            headers = null,
            scenarios = emptyList(),
            method = HttpMethod.POST,
            dynamicDomain = dynamicDomain
        )

        val result1 = dynamicDomain.verifyBody(contentType, body, expectedBody)
        val result2 = dynamicDomain.verifyBody(contentType, body2, expectedBody)

        assertTrue { result1.isEmpty() }
        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson.toString(),
            receivedBody = body2,
        ), result2[0])

    }

    @Test
    fun objectArrayVerificationTest() {

        val schemaJson =
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
        
        """.trimIndent()

        val contentType = "application/json"

        val expectedBody =
            ApiRequestBody(
                content = ContentOrSchema.ContentField(mapOf(
                    contentType to ContentOrSchema.SchemaObject(
                        schema = schemaJson
                    )
                )),
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
            params = null,
            body = expectedBody,
            path = listOf((PathParts("users", false))),
            headers = null,
            dynamicDomain = dynamicDomain,
            scenarios = emptyList(),
            method = HttpMethod.POST,
        )

        val result1 = dynamicDomain.verifyBody(contentType, body, expectedBody)
        val result2 = dynamicDomain.verifyBody(contentType, body2, expectedBody)

        assertTrue { result1.isEmpty() }
        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson.toString(),
            receivedBody = body2,
        ), result2[0])

    }

    @Test
    fun nullBodyVerificationTest() {

        val schemaJson =
            """
        {
            "type": "null"
        }
        
        """.trimIndent()

        val contentType = "application/json"

        val expectedBody =
            ApiRequestBody(
                content = ContentOrSchema.ContentField(mapOf(
                    contentType to ContentOrSchema.SchemaObject(
                        schema = schemaJson
                    )
                )),
                required = true
            )

        val body = ""

        val dynamicHandler = DynamicHandler(
            params = null,
            body = expectedBody,
            path = listOf((PathParts("users", false))),
            headers = null,
            dynamicDomain = dynamicDomain,
            scenarios = emptyList(),
            method = HttpMethod.POST,
        )

        val result1 = dynamicDomain.verifyBody(contentType, body, expectedBody)

        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson.toString(),
            receivedBody = body,
        ), result1[0])

    }

    @Test
    fun booleanBodyVerificationTest() {

        val schemaJson =
            """
        {
            "type": "boolean"
        }
        
        """.trimIndent()

        val contentType = "application/json"

        val expectedBody =
            ApiRequestBody(
                content = ContentOrSchema.ContentField(mapOf(
                    contentType to ContentOrSchema.SchemaObject(
                        schema = schemaJson
                    )
                )),
                required = true
            )

        val body = "true"
        val body2 = "false"
        val body3 = "null"

        val dynamicHandler = DynamicHandler(
            params = null,
            body = expectedBody,
            path = listOf((PathParts("users", false))),
            headers = null,
            dynamicDomain = dynamicDomain,
            scenarios = emptyList(),
            method = HttpMethod.POST,
        )

        val result1 = dynamicDomain.verifyBody(contentType, body, expectedBody)
        val result2 = dynamicDomain.verifyBody(contentType, body2, expectedBody)
        val result3 = dynamicDomain.verifyBody(contentType, body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2.isEmpty() }
        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson.toString(),
            receivedBody = body3,
        ), result3[0])

    }

    @Test
    fun numberBodyVerificationTest() {

        val schemaJson =
            """
        {
            "type": "number"
        }
        
        """.trimIndent()

        val contentType = "application/json"

        val expectedBody =
            ApiRequestBody(
                content = ContentOrSchema.ContentField(mapOf(
                    contentType to ContentOrSchema.SchemaObject(
                        schema = schemaJson
                    )
                )),
                required = true
            )

        val body = "3.14"
        val body2 = "3"
        val body3 = "null"

        val dynamicHandler = DynamicHandler(
            params = null,
            body = expectedBody,
            path = listOf((PathParts("users", false))),
            headers = null,
            dynamicDomain = dynamicDomain,
            scenarios = emptyList(),
            method = HttpMethod.POST,
        )

        val result1 = dynamicDomain.verifyBody(contentType, body, expectedBody)
        val result2 = dynamicDomain.verifyBody(contentType, body2, expectedBody)
        val result3 = dynamicDomain.verifyBody(contentType, body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2.isEmpty() }
        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson.toString(),
            receivedBody = body3,
        ), result3[0])

    }

    @Test
    fun stringBodyVerificationTest() {

        val schemaJson =
            """
        {
            "type": "string"
        }
        
        """.trimIndent()

        val contentType = "application/json"

        val expectedBody =
            ApiRequestBody(
                content = ContentOrSchema.ContentField(mapOf(
                    contentType to ContentOrSchema.SchemaObject(
                        schema = schemaJson
                    )
                )),
                required = true
            )

        val body = "\"hello\""
        val body2 = ""
        val body3 = "\"null\""

        val dynamicHandler = DynamicHandler(
            params = null,
            body = expectedBody,
            path = listOf((PathParts("users", false))),
            headers = null,
            dynamicDomain = dynamicDomain,
            scenarios = emptyList(),
            method = HttpMethod.POST,
        )

        val result1 = dynamicDomain.verifyBody(contentType, body, expectedBody)
        val result2 = dynamicDomain.verifyBody(contentType, body2, expectedBody)
        val result3 = dynamicDomain.verifyBody(contentType, body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson,
            receivedBody = body2,
        ), result2[0])
        assertTrue { result3.isEmpty() }

    }

    @Test
    fun integerBodyVerificationTest() {

        val schemaJson =
            """
        {
            "type": "integer"
        }
        
        """.trimIndent()

        val contentType = "application/json"

        val expectedBody =
            ApiRequestBody(
                content = ContentOrSchema.ContentField(mapOf(
                    contentType to ContentOrSchema.SchemaObject(
                        schema = schemaJson
                    )
                )),
                required = true
            )

        val body = "3"
        val body2 = "3.14"
        val body3 = "null"

        val dynamicHandler = DynamicHandler(
            params = null,
            body = expectedBody,
            path = listOf((PathParts("users", false))),
            headers = null,
            dynamicDomain = dynamicDomain,
            scenarios = emptyList(),
            method = HttpMethod.POST,
        )

        val result1 = dynamicDomain.verifyBody(contentType, body, expectedBody)
        val result2 = dynamicDomain.verifyBody(contentType, body2, expectedBody)
        val result3 = dynamicDomain.verifyBody(contentType, body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson.toString(),
            receivedBody = body2,
        ), result2[0])
        assertEquals(VerifyBodyError.InvalidBodyFormat(
            schemaJson.toString(),
            receivedBody = body3,
        ), result3[0])

    }

    @Test
    fun headersVerificationTest() {

        val expectedHeaders = listOf(
            ApiHeader(
                name = "Content-Type",
                type = ContentOrSchema.SchemaObject(
                    schema =
                        """
                        {
                            "type": "string"
                        }
                        """.trimIndent()
                ),
                required = true,
                style = ParameterStyle.FORM,
                explode = false,
                description = "Content type of the request"
            ),
            ApiHeader(
                name = "Authorization",
                type = ContentOrSchema.SchemaObject(
                    schema =
                        """
                        {
                            "type": "string"
                        }
                        """.trimIndent()
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
            params = null,
            body = null,
            path = listOf((PathParts("users", false))),
            headers = expectedHeaders,
            dynamicDomain = dynamicDomain,
            scenarios = emptyList(),
            method = HttpMethod.POST,
        )

        val result1 = dynamicDomain.verifyHeaders(headers, expectedHeaders, "application/json", false)
        val result2 = dynamicDomain.verifyHeaders(headers1, expectedHeaders, "application/json", false)
        val result3 = dynamicDomain.verifyHeaders(headers2, expectedHeaders, "application/json", false)
        val result4 = dynamicDomain.verifyHeaders(headers3, expectedHeaders, "application/json", false)
        val result5 = dynamicDomain.verifyHeaders(headers4, expectedHeaders, "application/json", false)

        assertTrue { result1.isEmpty() }
        assertTrue { result2.isEmpty() }

        assertTrue { result3.size == 2 }
        assertTrue { result3[0] == VerifyHeadersError.MissingHeader("Content-Type") }
        assertTrue { result3[1] == VerifyHeadersError.MissingHeaderContent("Content-Type") }

        assertTrue { result4.size == 1 }
        assertTrue { result4[0] == VerifyHeadersError.InvalidContentType("application/json", "application/problem") }

        assertTrue { result5.isEmpty() }

    }

    @Test
    fun cookiesVerificationTest() {

        val expectedCookies = listOf(
            ApiParameter(
                name = "a",
                location = Location.COOKIE,
                type = ContentOrSchema.ContentField(
                    content = mapOf(
                        Pair("text/plain", ContentOrSchema.SchemaObject(null))
                    )
                ),
                required = true,
                style = ParameterStyle.FORM,
                explode = false,
                allowEmptyValue = false,
                description = "Cookie"
            ),
            ApiParameter(
                name = "b",
                type = ContentOrSchema.SchemaObject(
                    schema =
                        """
                        {
                            "type": "integer"
                        }
                        """.trimIndent()
                ),
                required = false,
                style = ParameterStyle.FORM,
                explode = false,
                description = "Cookie",
                location = Location.COOKIE,
                allowEmptyValue = false
            )
        )

        val cookie1 = arrayOf(Cookie("a", "Bom dia"))

        val cookie2 = arrayOf(Cookie("a", "Bom dia"), Cookie("b", "123"))

        val cookie3 = arrayOf(Cookie("b", "asd"))

        val cookie4 = arrayOf(Cookie("a", ""))

        val cookie5 = arrayOf(Cookie("a", "Bom dia"), Cookie("c", "123"))

        val result1 = dynamicDomain.verifyCookies(cookie1, expectedCookies)
        val result2 = dynamicDomain.verifyCookies(cookie2, expectedCookies)
        val result3 = dynamicDomain.verifyCookies(cookie3, expectedCookies)
        val result4 = dynamicDomain.verifyCookies(cookie4, expectedCookies)
        val result5 = dynamicDomain.verifyCookies(cookie5, expectedCookies)

        assertTrue { result1.isEmpty() }
        assertEquals(emptyList(), result2)

        assertTrue { result3.size == 2 }
        assertEquals(
            VerifyParamsError.MissingParam(
                location = Location.COOKIE,
                paramName = "a"
            ),
            result3[0]
        )
        assertEquals(
            VerifyParamsError.JsonValidationError(
                location = Location.COOKIE,
            ),
            result3[1]
        )

        assertTrue { result4.size == 1 }
        assertEquals(
            VerifyParamsError.ParamCantBeEmpty(
                location = Location.COOKIE,
                paramName = "a"
            ),
            result4[0]
        )

        assertEquals(1, result5.size)
        assertEquals(
            VerifyParamsError.InvalidParam(
                location = Location.COOKIE,
                paramName = "c"
            ),
            result5[0]
        )
    }


    companion object {
        val dynamicDomain = DynamicDomain()
    }
}