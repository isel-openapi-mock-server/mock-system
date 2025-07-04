package isel.openapi.mock.http

import com.github.jknack.handlebars.Handlebars
import jakarta.servlet.http.HttpServletRequest
import isel.openapi.mock.domain.dynamic.DynamicDomain
import isel.openapi.mock.domain.dynamic.HandlerResult
import isel.openapi.mock.domain.openAPI.*
import isel.openapi.mock.domain.problems.ParameterInfo
import isel.openapi.mock.services.HandlebarsContext
import isel.openapi.mock.services.ResponseConfig
import isel.openapi.mock.services.Scenario
import org.apache.commons.text.StringEscapeUtils

interface VerificationError

sealed class VerifyBodyError: VerificationError {
    data class InvalidBodyFormat(val expectedBodyType: String, val receivedBody: String): VerifyBodyError()
    data class InvalidResponseBodyFormat(val expectedType: String, val receivedType: String): VerifyBodyError()
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
    private val scenario: Scenario?,
    private val responses: List<Response>
) {

    fun handle(
        request: HttpServletRequest,
        handlebars: Handlebars
    ): HandlerResult {

        val requestBody = request.reader.readText().ifBlank { null }
        val requestQueryParams = extractQueryParams(request)
        val requestPathParams = dynamicDomain.getPathParams(path, request.requestURI)
        val requestHeaders = extractHeaders(request)
        val cookies = request.cookies ?: emptyArray()
        val contentType = request.contentType

        val fails = mutableListOf<VerificationError>()

        if(body != null && requestBody != null) {
            fails.addAll(
                dynamicDomain.verifyBody(
                    contentType,
                    requestBody,
                    body
                )
            )
        }

        fails.addAll(
            dynamicDomain.verifyHeaders(
                requestHeaders,
                headers ?: emptyList(),
                contentType,
                security
            )
        )

        fails.addAll(
            dynamicDomain.verifyCookies(
                cookies,
                params?.filter { it.location == Location.COOKIE } ?: emptyList()
            )
        )

        val queryParamsResult = dynamicDomain.verifyQueryParams(
            requestQueryParams,
            params?.filter { it.location == Location.QUERY } ?: emptyList()
        )
        queryParamsResult.errors.forEach { fails.add(it) }

        val pathParamsResult = dynamicDomain.verifyPathParams(
            requestPathParams,
            params?.filter { it.location == Location.PATH } ?: emptyList()
        )
        pathParamsResult.errors.forEach { fails.add(it) }

        val response = if(fails.isEmpty()) scenario!!.getResponse() else null

        val processedBody = processResponseBody(
            response,
            request,
            requestBody,
            requestHeaders,
            pathParamsResult.params + queryParamsResult.params,
            handlebars
        )

        fails.addAll(
            dynamicDomain.verifyResponseBody(
                processedBody,
                response?.contentType ?: "application/json",
                responses.firstOrNull { r -> r.statusCode == response?.statusCode }?.schema
            )
        )

        return HandlerResult(
            fails = fails,
            body = requestBody,
            headers = requestHeaders,
            cookies = cookies.toList(),
            params = pathParamsResult.params + queryParamsResult.params,
            response = response,
            processedBody = processedBody
        )
    }

    fun hasScenario() : Boolean {
        return scenario != null
    }

    private fun extractHeaders(request: HttpServletRequest): Map<String, String> {
        return request.headerNames.toList().associateWith { request.getHeader(it) }
    }

    private fun extractQueryParams(request: HttpServletRequest): Map<String, List<Any>> {
        return request.parameterMap.mapValues { entry -> entry.value.map { value -> value.toTypedValue() } }
    }

    private fun processResponseBody(
        response: ResponseConfig?,
        request: HttpServletRequest,
        requestBody: String?,
        headers: Map<String, String>,
        params: List<ParameterInfo>,
        handlebars: Handlebars
    ): ByteArray? {
        val rawBody = response?.body ?: return null
        val bodyString = String(rawBody, Charsets.UTF_8)

        val unescaped = if (bodyString.contains("\\\"") ||
            bodyString.contains("\\u007b") ||
            bodyString.startsWith("\"[") ||
            bodyString.endsWith("]\"")
        ) {
            StringEscapeUtils.unescapeJson(bodyString)
        } else bodyString

        return if (unescaped.contains("{{")) {
            val context = HandlebarsContext()
                .addBody(requestBody, response.contentType ?: "application/json")
                .addUrl(request.requestURL.toString())
                .pathParts(request.requestURI)
                .addParams(params)
                .addHeaders(headers)

            val template = handlebars.compileInline(unescaped)
            template.apply(context.getContext()).toByteArray()
        } else unescaped.toByteArray()
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
