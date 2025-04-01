package isel.openapi.mock.http

import isel.openapi.mock.parsingServices.model.ApiParameter
import isel.openapi.mock.parsingServices.model.ApiRequestBody
import isel.openapi.mock.parsingServices.model.Location
import isel.openapi.mock.parsingServices.model.ParameterStyle
import isel.openapi.mock.parsingServices.model.PathParts
import isel.openapi.mock.parsingServices.model.Type
import kotlin.test.Test
import kotlin.test.assertTrue

class DynamicHandlersTests {

    @Test
    fun objectVerificationTest() {

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schemaType = Type.ObjectType(
                    fieldsTypes = mapOf(
                        "name" to Type.StringType,
                        "age" to Type.IntegerType
                    )
                ),
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

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users")))
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidBodyTypes("age", Type.IntegerType, Type.StringType) }
        assertTrue { result3[0] == VerifyBodyError.InvalidBodyFormat(Type.ObjectType(mapOf("name" to Type.StringType, "age" to Type.IntegerType))) }

    }

    @Test
    fun arrayVerificationTest() {

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schemaType = Type.ArrayType(
                    elementsType = Type.IntegerType
                ),
                required = true
            )

        val body = """
            [1,2,3,4]
        """.trimIndent()
        val body2 = """
            [1,2,3,"4"]
        """.trimIndent()

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users")))
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidArrayElement(Type.IntegerType, Type.StringType) }

    }

    @Test
    fun objectArrayVerificationTest() {

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schemaType = Type.ArrayType(
                    elementsType = Type.ObjectType(
                        fieldsTypes = mapOf(
                            "name" to Type.StringType,
                            "age" to Type.IntegerType
                        )
                    )
                ),
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

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users")))
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidArrayElement(
            Type.ObjectType(mapOf("name" to Type.StringType, "age" to Type.IntegerType)),
            Type.ObjectType(mapOf("name" to Type.StringType, "age" to Type.StringType)))
        }

    }

    @Test
    fun nullBodyVerificationTest() {

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schemaType = Type.NullType,
                required = true
            )

        val body = ""
        val body2 = "null"

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users")))
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidBodyFormat(Type.NullType) }

    }

    @Test
    fun booleanBodyVerificationTest() {

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schemaType = Type.BooleanType,
                required = true
            )

        val body = "true"
        val body2 = "false"
        val body3 = "null"

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users")))
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2.isEmpty() }
        assertTrue { result3[0] == VerifyBodyError.InvalidBodyFormat(Type.BooleanType) }

    }

    @Test
    fun numberBodyVerificationTest() {

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schemaType = Type.NumberType,
                required = true
            )

        val body = "3.14"
        val body2 = "3"
        val body3 = "null"

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users")))
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2.isEmpty() }
        assertTrue { result3[0] == VerifyBodyError.InvalidBodyFormat(Type.NumberType) }

    }

    @Test
    fun stringBodyVerificationTest() {

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schemaType = Type.StringType,
                required = true
            )

        val body = "Hello"
        val body2 = ""
        val body3 = "null"

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users")))
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidBodyFormat(Type.StringType) }
        assertTrue { result3.isEmpty() }

    }

    @Test
    fun integerBodyVerificationTest() {

        val expectedBody =
            ApiRequestBody(
                contentType = "application/json",
                schemaType = Type.IntegerType,
                required = true
            )

        val body = "3"
        val body2 = "3.14"
        val body3 = "null"

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody,
            path = listOf((PathParts.Static("users")))
        )

        val result1 = dynamicHandler.verifyBody(body, expectedBody)
        val result2 = dynamicHandler.verifyBody(body2, expectedBody)
        val result3 = dynamicHandler.verifyBody(body3, expectedBody)

        assertTrue { result1.isEmpty() }
        assertTrue { result2[0] == VerifyBodyError.InvalidBodyFormat(Type.IntegerType) }
        assertTrue { result3[0] == VerifyBodyError.InvalidBodyFormat(Type.IntegerType) }

    }

    @Test
    fun headersVerificationTest() {

        val expectedHeaders = listOf(
            ApiParameter(
                name = "Content-Type",
                location = Location.HEADER,
                type = Type.StringType,
                required = true,
                allowEmptyValue = false,
                style = ParameterStyle.FORM,
                explode = false,
                description = "Content type of the request"
            ),
            ApiParameter(
                name = "Authorization",
                location = Location.HEADER,
                type = Type.StringType,
                required = false,
                allowEmptyValue = false,
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

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = expectedHeaders,
            body = null,
            path = listOf((PathParts.Static("users")))
        )

        val result1 = dynamicHandler.verifyHeaders(headers, expectedHeaders, "application/json")
        val result2 = dynamicHandler.verifyHeaders(headers1, expectedHeaders, "application/json")
        val result3 = dynamicHandler.verifyHeaders(headers2, expectedHeaders, "application/json")
        val result4 = dynamicHandler.verifyHeaders(headers3, expectedHeaders, "application/json")
        val result5 = dynamicHandler.verifyHeaders(headers4, expectedHeaders, "application/json")

        assertTrue { result1.isEmpty() }
        assertTrue { result2.isEmpty() }

        assertTrue { result3.size == 3 }
        assertTrue { result3[0] == VerifyHeadersError.MissingHeader("Content-Type") }
        assertTrue { result3[1] == VerifyHeadersError.MissingHeaderContent("Content-Type") }
        assertTrue { result3[2] == VerifyHeadersError.InvalidContentType("application/json", "") }

        assertTrue { result4.size == 1 }
        assertTrue { result4[0] == VerifyHeadersError.InvalidContentType("application/json", "application/problem") }

        assertTrue { result5.size == 1 }
        assertTrue { result5[0] == VerifyHeadersError.InvalidHeader(setOf("Extra")) }

    }

}