package isel.openapi.admin.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import isel.openapi.admin.domain.admin.AdminDomain
import isel.openapi.admin.domain.admin.VerifyResponseError
import isel.openapi.admin.parsing.model.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminDomainTests {

    private val adminDomain = AdminDomain()

    @Test
    fun `generateHost should return a 16-character alphanumeric string`() {
        val host = adminDomain.generateHost()
        assertEquals(16, host.length)
        assertTrue(host.all { it.isLetterOrDigit() })
    }

    @Test
    fun `generateTokenValue should return a non-empty Base64-encoded string`() {
        val token = adminDomain.generateTokenValue()
        assertDoesNotThrow {
            Base64.getUrlDecoder().decode(token)
        }
    }

    @Test
    fun `verifyResponse should detect wrong status code`() {
        val responseSpec = Response(statusCode = StatusCode.OK, headers = emptyList(), schema = null)
        val errors = adminDomain.verifyResponse(responseSpec, StatusCode.NOT_FOUND, null, null, null)
        assertEquals(listOf(VerifyResponseError.WrongStatusCode), errors)
    }


    @Test
    fun `verifyResponse should detect missing header`() {
        val headerName = "header1"
        val headers = listOf(
            ApiHeader(
                name = headerName,
                description = "",
                required = true,
                style = ParameterStyle.FORM,
                explode = false,
                type = ContentOrSchema.SchemaObject(
                    schema = null
                )
            )
        )
        val responseSpec = Response(statusCode = StatusCode.OK, headers = headers, schema = null)
        val errors = adminDomain.verifyResponse(responseSpec, StatusCode.OK, null, null, null)
        assertEquals(listOf(VerifyResponseError.MissingHeader(name = headerName)), errors)
    }

    @Test
    fun `verifyResponse should detect invalid type in header`() {
        val headerName = "header1"
        val type = """
                    {
                        "type": "boolean"
                    }                    
                    """.trimIndent()
        val headers = listOf(
            ApiHeader(
                name = headerName,
                description = "",
                required = true,
                style = ParameterStyle.FORM,
                explode = false,
                type = ContentOrSchema.SchemaObject(
                    schema = type
                )
            )
        )
        val responseSpec = Response(statusCode = StatusCode.OK, headers = headers, schema = null)
        val givenHeader = mapOf(Pair("header1","bom dia"))
        val errors = adminDomain.verifyResponse(responseSpec, StatusCode.OK, null, givenHeader, null)
        assertEquals(listOf(VerifyResponseError.InvalidType(name = headerName, expectedType = type, givenType = isel.openapi.admin.domain.Type.StringType.toString())), errors)
    }

    @Test
    fun `verifyResponse should detect invalid header`() {
        val headerName = "header1"
        val responseSpec = Response(statusCode = StatusCode.OK, schema = null)
        val givenHeader = mapOf(Pair(headerName, "true"))
        val errors = adminDomain.verifyResponse(responseSpec, StatusCode.OK, null, givenHeader, null)
        assertEquals(listOf(VerifyResponseError.InvalidHeader(name = headerName)), errors)
    }
/*
    @Test
    fun `verifyResponse should detect invalid body schema`() {
        val type = """
                    {
                        "type": "boolean"
                    }                    
                    """.trimIndent()
        val body = ContentOrSchema.ContentField(
            content = mapOf(
                Pair(
                    "application/json",
                    ContentOrSchema.SchemaObject(
                        schema = type
                    )
                )
            )
        )
        val responseSpec = Response(statusCode = StatusCode.OK, schema = body)
        val givenBody = "bom dia".toByteArray()
        val errors = adminDomain.verifyResponse(responseSpec, StatusCode.OK, "application/json", null, givenBody)
        assertEquals(listOf(VerifyResponseError.InvalidBodyFormat(expectedFormat = type, givenFormat = givenBody)), errors)
    }
*/

    @Test
    fun `verifyResponse should be a success`() {
        val headerName = "header1"
        val headerType = """
                    {
                        "type": "string"
                    }                    
                    """.trimIndent()
        val headers = listOf(
            ApiHeader(
                name = headerName,
                description = "",
                required = true,
                style = ParameterStyle.FORM,
                explode = false,
                type = ContentOrSchema.SchemaObject(
                    schema = headerType
                )
            )
        )
        val bodyType = """
                    {
                        "type": "string"
                    }                    
                    """.trimIndent()
        val body = ContentOrSchema.ContentField(
            content = mapOf(
                Pair(
                    "application/json",
                    ContentOrSchema.SchemaObject(
                        schema = bodyType
                    )
                )
            )
        )
        val responseSpec = Response(statusCode = StatusCode.OK, schema = body, headers = headers)
        val givenHeader = mapOf(Pair(headerName,"bom dia"))
        val givenBody = "ola".toByteArray()
        val errors = adminDomain.verifyResponse(responseSpec, StatusCode.OK, "application/json", givenHeader, givenBody)
        assertEquals(emptyList(), errors)
    }
}
