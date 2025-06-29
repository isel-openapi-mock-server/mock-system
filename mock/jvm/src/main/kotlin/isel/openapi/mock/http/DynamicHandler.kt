package isel.openapi.mock.http

import jakarta.servlet.http.HttpServletRequest
import isel.openapi.mock.domain.dynamic.DynamicDomain
import isel.openapi.mock.domain.dynamic.HandlerResult
import isel.openapi.mock.domain.openAPI.*
import isel.openapi.mock.domain.problems.ParameterInfo
import isel.openapi.mock.services.ResponseConfig
import isel.openapi.mock.services.Scenario

interface VerificationError

sealed class VerifyBodyError: VerificationError {
    data class InvalidBodyFormat(val expectedBodyType: String, val receivedBody: String): VerifyBodyError()
}

sealed class VerifyHeadersError: VerificationError {
    data class InvalidType(val headerKey: String, val expectedType: String, val receivedType: String): VerifyHeadersError()
    data class InvalidContentType(val expectedType: String, val receivedType: String): VerifyHeadersError()
    data class MissingHeader(val expectedHeader: String): VerifyHeadersError()
    data class MissingHeaderContent(val headerKey: String): VerifyHeadersError()
}

sealed class VerifyParamsError : VerificationError {
    data class InvalidType(val name: String, val location: Location, val expectedType: String, val receivedType: String) : VerifyParamsError()
    data class ParamCantBeEmpty(val location: Location, val paramName: String) : VerifyParamsError()
    data class InvalidParam(val location: Location, val paramName: String) : VerifyParamsError()
    data class MissingParam(val location: Location, val paramName: String) : VerifyParamsError()
    data class JsonValidationError(val location: Location) : VerifyParamsError()
}

class DynamicHandler(
    private val path: List<PathParts>,
    private val method: HttpMethod,
    private val params: List<ApiParameter>?,
    private val body: ApiRequestBody?,
    private val headers : List<ApiHeader>?,
    private val security: Boolean = false,
    private val dynamicDomain: DynamicDomain,
    private val scenarios: List<Scenario>,
) {

    fun handle(
        request: HttpServletRequest,
        scenarioName: String
    ): HandlerResult {

        val requestBody = request.reader.readText().ifBlank { null }
        val requestQueryParams = request.parameterMap.mapValues { entry -> entry.value.map { value -> value.toTypedValue() } }
        val requestPathParams = dynamicDomain.getPathParams(path, request.requestURI)
        val requestHeaders = request.headerNames.toList().associateWith { request.getHeader(it) }
        val cookies = request.cookies ?: emptyArray()

        //val currentParameters = mutableListOf<ParameterInfo>()

        val contentType = request.contentType

        val fails = mutableListOf<VerificationError>()

        if(body != null && requestBody != null) {
            val bodyResult = dynamicDomain.verifyBody(contentType, requestBody, body)
            bodyResult.forEach { fails.add(it) }
        }

        val headersResult = dynamicDomain.verifyHeaders(requestHeaders, headers ?: emptyList(), contentType, security)
        headersResult.forEach { fails.add(it) }

        val queryParamsResult = dynamicDomain.verifyQueryParams(requestQueryParams, params?.filter { it.location == Location.QUERY } ?: emptyList())
        queryParamsResult.errors.forEach { fails.add(it) }

        val pathParamsResult = dynamicDomain.verifyPathParams(requestPathParams, params?.filter { it.location == Location.PATH } ?: emptyList())
        pathParamsResult.errors.forEach { fails.add(it) }

        val cookiesResult = dynamicDomain.verifyCookies(cookies, params?.filter { it.location == Location.COOKIE } ?: emptyList())
        cookiesResult.forEach { fails.add(it) }

        val response = if(fails.isEmpty()) scenarios.find { it.name == scenarioName }?.getResponse() else null

        return HandlerResult(
            fails = fails,
            body = requestBody,
            headers = requestHeaders,
            cookies = cookies.toList(),
            params = pathParamsResult.params + queryParamsResult.params,
            response = response
        )
    }

    private fun String.toTypedValue(): Any {
        return when {
            toIntOrNull() != null -> toInt()    // Integer
            toDoubleOrNull() != null -> toDouble()  // Number
            equals("true", ignoreCase = true) || equals("false", ignoreCase = true) -> toBoolean() // Boolean
            else -> this // String
        }
    }

}
