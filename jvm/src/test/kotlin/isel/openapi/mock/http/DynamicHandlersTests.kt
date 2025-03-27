package isel.openapi.mock.http

import isel.openapi.mock.parsingServices.model.ApiParameter
import isel.openapi.mock.parsingServices.model.ApiRequestBody
import isel.openapi.mock.parsingServices.model.Location
import isel.openapi.mock.parsingServices.model.ParameterStyle
import isel.openapi.mock.parsingServices.model.Type
import isel.openapi.mock.utils.Failure
import isel.openapi.mock.utils.Success
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
            body = expectedBody
        )

        assertTrue { dynamicHandler.verifyBody(body, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body2, expectedBody) is Failure }
        assertTrue { dynamicHandler.verifyBody(body3, expectedBody) is Failure }

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
            body = expectedBody
        )

        assertTrue { dynamicHandler.verifyBody(body, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body2, expectedBody) is Failure }

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
            body = expectedBody
        )

        assertTrue { dynamicHandler.verifyBody(body, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body2, expectedBody) is Failure }

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
            body = expectedBody
        )

        assertTrue { dynamicHandler.verifyBody(body, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body2, expectedBody) is Failure }
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
            body = expectedBody
        )

        assertTrue { dynamicHandler.verifyBody(body, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body2, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body3, expectedBody) is Failure }
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
            body = expectedBody
        )

        assertTrue { dynamicHandler.verifyBody(body, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body2, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body3, expectedBody) is Failure }
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
            body = expectedBody
        )

        assertTrue { dynamicHandler.verifyBody(body, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body2, expectedBody) is Failure }
        assertTrue { dynamicHandler.verifyBody(body3, expectedBody) is Success }
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
            body = expectedBody
        )

        assertTrue { dynamicHandler.verifyBody(body, expectedBody) is Success }
        assertTrue { dynamicHandler.verifyBody(body2, expectedBody) is Failure }
        assertTrue { dynamicHandler.verifyBody(body3, expectedBody) is Failure }
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
            body = null
        )

        assertTrue { dynamicHandler.verifyHeaders(headers, expectedHeaders, "application/json") is Success }
        assertTrue { dynamicHandler.verifyHeaders(headers1, expectedHeaders, "application/json") is Success }
        assertTrue { dynamicHandler.verifyHeaders(headers2, expectedHeaders, "application/json") is Failure }
        assertTrue { dynamicHandler.verifyHeaders(headers3, expectedHeaders, "application/json") is Failure }
        assertTrue { dynamicHandler.verifyHeaders(headers4, expectedHeaders, "application/json") is Failure }

    }

}