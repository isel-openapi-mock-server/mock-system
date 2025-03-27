package isel.openapi.mock.http

import isel.openapi.mock.parsingServices.model.ApiRequestBody
import isel.openapi.mock.parsingServices.model.Type
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DynamicHandlersTests {

    @Test
    fun verifyBodyTest() {

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

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody
        )

        assertTrue { dynamicHandler.verifyBody(body, expectedBody) }

    }

    @Test
    fun verifyBodyTest2() {

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
                "age": "30"
            }
        """.trimIndent()

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody
        )

        assertFailsWith<IllegalArgumentException> { dynamicHandler.verifyBody(body, expectedBody) }

    }

    @Test
    fun verifyBodyTest3() {

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
            bom dia
        """.trimIndent()

        val dynamicHandler = BodyAndParamsDynamicHandler(
            response = "Response",
            params = null,
            body = expectedBody
        )

        assertFailsWith<IllegalArgumentException> { dynamicHandler.verifyBody(body, expectedBody) }

    }

}