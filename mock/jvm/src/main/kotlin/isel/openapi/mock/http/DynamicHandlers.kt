package isel.openapi.mock.http

import jakarta.servlet.http.HttpServletRequest
import com.github.erosb.jsonsKema.*
import io.swagger.util.Json
import isel.openapi.mock.parsingServices.model.*
import jakarta.servlet.http.Cookie

interface VerificationError

sealed class VerifyBodyError: VerificationError {
    data class InvalidBodyFormat(val expectedBodyType: String): VerifyBodyError()
}

sealed class VerifyHeadersError: VerificationError {
    data class InvalidType(val headerKey: String, val expectedType: String, val receivedType: String): VerifyHeadersError()
    data class InvalidContentType(val expectedType: String, val receivedType: String): VerifyHeadersError()
    data class MissingHeader(val expectedHeaders: String): VerifyHeadersError()
    data class MissingHeaderContent(val headerKey: String): VerifyHeadersError()
}

sealed class VerifyParamsError : VerificationError {
    data class InvalidType(val location: Location, val expectedType: String, val receivedType: String) : VerifyParamsError()
    data class ParamCantBeEmpty(val location: Location, val paramName: String) : VerifyParamsError()
    data class InvalidParam(val location: Location, val paramName: String) : VerifyParamsError()
    data class MissingParam(val location: Location, val paramName: String) : VerifyParamsError()
}

class HandlerResult(
    val fails: List<VerificationError>,
    val response: Response
)

class DynamicHandler(
    private val path: List<PathParts>,
    private val response: List<Response>,
    private val params: List<ApiParameter>?,
    private val body: ApiRequestBody?,
    private val headers : List<ApiHeader>?,
    private val security: Boolean = false,
) {

    fun handle(
        request: HttpServletRequest,
    ): HandlerResult {
        val requestBody = request.reader.readText().ifBlank { null }
        val requestQueryParams = request.parameterMap.mapValues { entry -> entry.value.map { value -> value.toTypedValue() } } // Mudei de it.value[0], para it.value
        val requestPathParams = getPathParams(request.requestURI)
        val requestHeaders = request.headerNames.toList().associateWith { request.getHeader(it) }
        val cookies = request.cookies ?: emptyArray()

        val contentType = request.contentType

        val fails = mutableListOf<VerificationError>()

        if(body != null && requestBody != null) {
            val bodyResult = verifyBody(contentType, requestBody, body)
            bodyResult.forEach { fails.add(it) }
        }

        val headersResult = verifyHeaders(requestHeaders, headers ?: emptyList(), contentType, security)
        headersResult.forEach { fails.add(it) }

        val queryParamsResult = verifyQueryParams(requestQueryParams, params?.filter { it.location == Location.QUERY } ?: emptyList())
        queryParamsResult.forEach { fails.add(it) }

        val pathParamsResult = verifyPathParams(requestPathParams, params?.filter { it.location == Location.PATH } ?: emptyList())
        pathParamsResult.forEach { fails.add(it) }

        val cookiesResult = verifyCookies(cookies, params?.filter { it.location == Location.COOKIE } ?: emptyList())
        cookiesResult.forEach { fails.add(it) }

        val response = if(fails.isEmpty()) {
            response.firstOrNull { it.statusCode == StatusCode.OK } ?: response.first()
        } else {
            Response(
                statusCode = StatusCode.BAD_REQUEST,
                contentType = "application/json",
                schema = JsonParser(
                    """
                    {
                        "type": "null"
                    }
                    """.trimIndent()
                ).parse()
            )
        }
        return HandlerResult(fails, response)
    }

    private fun getPathParams(
        uri: String
    ): Map<String, Any> {
        val parts = uri.split("/").filter{ it.isNotBlank() }.ifEmpty { return emptyMap() }
        val pathParams = mutableMapOf<String, Any>()
        path.forEachIndexed{ idx, part ->
            if (part is PathParts.Param) {
                pathParams[part.name] = parts[idx].toTypedValue()
            }
        }
        return pathParams
    }

    private fun String.toTypedValue(): Any {
        return when {
            toIntOrNull() != null -> toInt()    // Integer
            toDoubleOrNull() != null -> toDouble()  // Number
            equals("true", ignoreCase = true) || equals("false", ignoreCase = true) -> toBoolean() // Boolean
            else -> this // String
        }
    }

    private fun verifyQueryParams(
        queryParams: Map<String, List<Any>>,
        expectedQueryParams: List<ApiParameter>
    ): List<VerifyParamsError> {

        val failList = mutableListOf<VerifyParamsError>()

        if (expectedQueryParams.isNotEmpty() && expectedQueryParams.any { it.required }  && queryParams.isEmpty()) {
            val missingParams = expectedQueryParams.filter { it.required }
            missingParams.forEach { param ->
                failList.add(VerifyParamsError.MissingParam(Location.QUERY, param.name))
            }
        }

        if (expectedQueryParams.isEmpty() && queryParams.isNotEmpty()) {
            queryParams.keys.forEach { key ->
                failList.add(VerifyParamsError.InvalidParam(Location.QUERY, key))
            }
        }

        if(failList.isNotEmpty()) return failList

        expectedQueryParams.forEach { expectedParam ->

            if(expectedParam.required && !queryParams.containsKey(expectedParam.name)) {
                failList.add(VerifyParamsError.MissingParam(expectedParam.location,  expectedParam.name))
            }

            val parameterValues = queryParams[expectedParam.name]
            if (parameterValues == null && expectedParam.required) {
                failList.add(VerifyParamsError.MissingParam(Location.QUERY, expectedParam.name))
                return@forEach
            }

            if (parameterValues != null) {
                for (value in parameterValues) {

                    val valueType = convertToType(value)

                    if(valueType is Type.StringType && (value as String).isBlank() && !expectedParam.allowEmptyValue) {
                        failList.add(VerifyParamsError.ParamCantBeEmpty(Location.QUERY, expectedParam.name))
                        return@forEach
                    }

                    validateContentOrSchema(
                        expectedParam.type,
                        valueType,
                        value,
                        VerifyParamsError.InvalidType(expectedParam.location, expectedParam.type.toString(), convertToType(value).toString())
                    )?.let { failList.add(it as VerifyParamsError) }
                }
            }
        }
        return failList
    }

    private fun validateContentOrSchema(
        contentOrSchema: ContentOrSchema,
        valueType: Type,
        value: Any,
        error: VerificationError
    ): VerificationError? {
        when(contentOrSchema) {
            is ContentOrSchema.SchemaObject -> {
                val currentValue = if(valueType is Type.StringType) "\"$value\"" else value.toString()
                val validationResult = jsonValidator(contentOrSchema.schema , currentValue )
                if(validationResult != null) {
                    return error
                }
            }
            is ContentOrSchema.ContentField -> {
                val contentField = contentOrSchema.content.entries.first().value.schema
                val currentValue = if(valueType is Type.StringType) "\"$value\"" else value.toString()
                if(contentField == null) {
                    return error
                }
                val validationResult = jsonValidator(contentField, currentValue)
                if(validationResult != null) {
                    return error
                }
            }
        }
        return null
    }

    private fun verifyPathParams(
        pathParams: Map<String, Any>,
        expectedPathParams: List<ApiParameter>
    ): List<VerifyParamsError> {

        val failList = mutableListOf<VerifyParamsError>()

        if (expectedPathParams.isNotEmpty() && expectedPathParams.any { it.required }  && pathParams.isEmpty()) {
            val missingParams = expectedPathParams.filter { it.required }
            missingParams.forEach { param ->
                failList.add(VerifyParamsError.MissingParam(Location.PATH, param.name))
            }
        }

        if (expectedPathParams.isEmpty() && pathParams.isNotEmpty()) {
            pathParams.keys.forEach { key ->
                failList.add(VerifyParamsError.InvalidParam(Location.PATH, key))
            }
        }

        if (failList.isNotEmpty()) return failList

        expectedPathParams.forEach { expectedParam ->
            if(expectedParam.required && !pathParams.containsKey(expectedParam.name)) {
                failList.add(VerifyParamsError.MissingParam(expectedParam.location,  expectedParam.name))
            }

            val paramValue = pathParams[expectedParam.name]

            if(paramValue == null && expectedParam.required) {
                failList.add(VerifyParamsError.MissingParam(expectedParam.location,  expectedParam.name))
            }

            val type = convertToType(paramValue)

            validateContentOrSchema(
                expectedParam.type,
                type,
                paramValue ?: "",
                VerifyParamsError.InvalidType(expectedParam.location, expectedParam.type.toString(), convertToType(paramValue).toString())
            )?.let { failList.add(it as VerifyParamsError) }
        }
        return failList
    }

    private fun verifyCookies(
        cookies: Array<Cookie>,
        expectedCookies: List<ApiParameter>,
    ): List<VerifyParamsError> {

        val failList = mutableListOf<VerifyParamsError>()

        if (expectedCookies.isNotEmpty() && expectedCookies.any { it.required } && cookies.isEmpty()) {
            val missingParams = expectedCookies.filter { it.required }
            missingParams.forEach { cookie ->
                failList.add(VerifyParamsError.MissingParam(Location.COOKIE, cookie.name))
            }
        }

        if (expectedCookies.isEmpty() && cookies.isNotEmpty()) {
            cookies.forEach { cookie ->
                failList.add(VerifyParamsError.InvalidParam(Location.COOKIE, cookie.name))
            }
        }

        if (failList.isNotEmpty()) return failList

        expectedCookies.forEach { expCookie ->

            val cookie = cookies.firstOrNull{ it.name == expCookie.name }

            // O paramametro esperado não vem no pedido
            if (cookie == null) {
                // O parametro é necessário.
                if (expCookie.required) {
                    failList.add(VerifyParamsError.MissingParam(Location.COOKIE, expCookie.name))
                }
                return@forEach
            }

            val type = expCookie.type

            var schema: JsonValue? = null

            if (type is ContentOrSchema.ContentField) {
                schema = type.content.entries.first().value.schema //Apenas uma entrada no content para parametros.
            } else if (type is ContentOrSchema.SchemaObject) {
                schema = type.schema
            }

            jsonValidator(schema, cookie.value)

        }

        return failList


    }

    fun verifyHeaders(
        headers: Map<String, String>,
        expectedHeaders: List<ApiHeader>,
        contentType: String?,
        security: Boolean
    ): List<VerifyHeadersError> {

        val failList = mutableListOf<VerifyHeadersError>()

        expectedHeaders.forEach { expectedHeader ->
            if(expectedHeader.required && !headers.containsKey(expectedHeader.name)) {
                failList.add(VerifyHeadersError.MissingHeader(expectedHeader.name))
            }

            val headerValue = headers[expectedHeader.name]

            if(headerValue == null && expectedHeader.required) {
                failList.add(VerifyHeadersError.MissingHeaderContent(expectedHeader.name))
            }

            when(val headerType = expectedHeader.type) {
                is ContentOrSchema.SchemaObject -> {
                    val validationResult = jsonValidator(headerType.schema , "\"$headerValue\"" )
                    if(validationResult != null) {
                        failList.add(VerifyHeadersError.InvalidType(expectedHeader.name, expectedHeader.type.toString(), convertToType(headerValue).toString()))
                    }
                }
                is ContentOrSchema.ContentField -> {
                    val contentField = headerType.content[contentType]
                    if(contentField == null) {
                        failList.add(VerifyHeadersError.InvalidType(expectedHeader.name, expectedHeader.type.toString(), convertToType(headerValue).toString()))
                    }
                    val validationResult = jsonValidator(contentField?.schema, "\"$headerValue\"")
                    if(validationResult != null) {
                        failList.add(VerifyHeadersError.InvalidType(expectedHeader.name, expectedHeader.type.toString(), convertToType(headerValue).toString()))
                    }
                }
            }
        }
        if(contentType != null && headers["Content-Type"] != null) {
            if(headers["Content-Type"] != contentType) {
                failList.add(VerifyHeadersError.InvalidContentType(contentType, headers["Content-Type"] ?: ""))
            }
        }
        if(security && headers["Authorization"] == null) {
            failList.add(VerifyHeadersError.MissingHeader("Authorization"))
        }
        if(security && headers["Authorization"] != null) {
            val authHeader = headers["Authorization"] ?: ""
            if(!authHeader.startsWith("Bearer ")) {
                failList.add(VerifyHeadersError.InvalidType("Authorization", "Bearer Token", authHeader))
            } else if(authHeader.substringAfter("Bearer ").trim().length <= 30) {
                failList.add(VerifyHeadersError.InvalidType("Authorization", "Bearer Token", authHeader))
            }
        }
        return failList
    }

    private fun jsonValidator(
        schema: JsonValue?,
        receivedType: String,
    ): ValidationFailure? {

        if(schema == null) { return null }

        val schemaLoader = SchemaLoader(schema)
        val validator = Validator.create(schemaLoader.load(), ValidatorConfig(FormatValidationPolicy.ALWAYS))

        val receivedBody = JsonParser(receivedType).parse()
        val validationResult = validator.validate(receivedBody)

        return validationResult
    }

    fun verifyBody(
        contentType: String,
        body: String,
        expectedBody: ApiRequestBody
    ): List<VerifyBodyError> {
        val failList = mutableListOf<VerifyBodyError>()
        try {
            expectedBody.content.content.forEach { (key, value) ->
                if (key == contentType) {
                    val validationResult = jsonValidator(value.schema, body)
                    if(validationResult != null) {
                        failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.content.toString()))
                    }
                }
            }
        } catch (e: Exception) {
            failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.content.toString()))
        }
        return failList
    }

    private fun convertToType(value: Any?): Type {
        return when (value) {
            null -> Type.NullType
            is Boolean -> Type.BooleanType
            is Int -> Type.IntegerType
            is Number -> Type.NumberType
            is String -> Type.StringType
            is List<*> -> {
                val elementType = convertToType(value.firstOrNull())
                Type.ArrayType(elementType)
            }
            is Map<*, *> -> {
                val fieldsTypes = value.map { (key, value) ->
                    if(key !is String) throw IllegalArgumentException("Invalid key type")
                    key to convertToType(value)
                }.toMap()
                Type.ObjectType(fieldsTypes)
            }
            else -> Type.UnknownType
        }
    }
}
