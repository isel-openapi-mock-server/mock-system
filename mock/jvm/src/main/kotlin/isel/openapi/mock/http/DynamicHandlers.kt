package isel.openapi.mock.http

import jakarta.servlet.http.HttpServletRequest
import com.github.erosb.jsonsKema.*
import isel.openapi.mock.parsingServices.model.*
import jakarta.servlet.http.Cookie

// TODO Fazer com que todas as sealed classes/intefaces implementem esta.
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

// TODO separar nos tipos de params diferentes (query, path e cookies).
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

        val headersResult = verifyHeaders(requestHeaders, headers ?: emptyList(), contentType)
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

        expectedQueryParams.forEach { param ->
            val values = queryParams[param.name]
            // O paramametro esperado não vem no pedido
            if (values == null) {
                // O parametro é necessário.
                if (param.required) {
                    failList.add(VerifyParamsError.MissingParam(Location.QUERY, param.name))
                }
                return@forEach
            }

            for (value in values) {
                val valueType = convertToType(value)
                // Verificar se nao tem valor(String vazia).
                if (
                    valueType == Type.StringType &&
                    (value as String).isBlank()
                ) {
                    // O param não suporta valor a vazio.
                    if (!param.allowEmptyValue) {
                        failList.add(VerifyParamsError.ParamCantBeEmpty(Location.QUERY, param.name))
                    }
                    return@forEach
                }

                if (valueType != param.type) {
                    failList.add(VerifyParamsError.InvalidType(Location.QUERY, param.type.toString(), valueType.toString()))
                }
            }
        }

        return failList

    }

    private fun verifyPathParams(
        pathParams: Map<String, Any>,
        expectedPathParams: List<ApiParameter>
    ): List<VerifyParamsError> {
        val failList = mutableListOf<VerifyParamsError>()

        if (expectedPathParams.isNotEmpty() && pathParams.isEmpty()) {
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


        expectedPathParams.forEach { param ->
            val value = pathParams[param.name]

            // O paramametro esperado não vem no pedido
            if (value == null) {
                // O parametro é necessário.
                if (param.required) {
                    failList.add(VerifyParamsError.MissingParam(Location.PATH, param.name))
                }
                return@forEach
            }
            val valueType = convertToType(value)

            // Verificar se nao tem valor, String vazia.
            if (
                valueType == Type.StringType &&
                (value as String).isBlank()
            ) {
                // O param não o suporta valor a vazio.
                if (!param.allowEmptyValue) {
                    failList.add(VerifyParamsError.ParamCantBeEmpty(Location.PATH, param.name))
                }
                return@forEach
            }
            if (valueType != param.type) {
                failList.add(VerifyParamsError.InvalidType(Location.PATH, param.type.toString(), valueType.toString()))
            }
        }
        return failList
    }

    private fun verifyCookies(
        cookies: Array<Cookie>,
        expectedCookies: List<ApiParameter>,
    ): List<VerifyParamsError> {

        var failed = false
        val failList = mutableListOf<VerifyParamsError>()

        if (expectedCookies.isNotEmpty() && cookies.isEmpty()) {
            failed = true
            // TODO adicionar erro à failList, para depois guardarmos os erros.
        }

        if (expectedCookies.isEmpty() && cookies.isNotEmpty()) {
            failed = true
            // TODO ERRO, adcionar à lista
        }

        if (failed) return failList

        expectedCookies.forEach { expCookie ->

            val cookie = cookies.firstOrNull{ it.name == expCookie.name }

            // O paramametro esperado não vem no pedido
            if (cookie == null) {
                // O parametro é necessário.
                if (expCookie.required) {
                    failed = true
                    // TODO Erro, adicionar a lista
                }
                return@forEach
            }

            val value = cookie.value

            val valueType = convertToType(value) // TODO ?????

            // Verificar se nao tem valor, String vazia.
            if (
                valueType == Type.StringType &&
                (value as String).isBlank()
            ) {
                // O cookie não o suporta valor a vazio.
                if (!expCookie.allowEmptyValue) {
                    failed = true
                    // TODO erro, o valor do cookie é vazio mas o cookie nao pode vir vazio
                }

                // avança para o proximo ciclo do forEach.
                return@forEach

            }

            if (valueType != expCookie.type) {
                failed = true
                // TODO Erro, adicionar a lista
            }

        }

        return if (failed) failList
        else emptyList()

    }

    fun verifyHeaders(
        headers: Map<String, String>,
        expectedHeaders: List<ApiHeader>,
        contentType: String
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
                    val validationResult = jsonValidator(headerType.schema , headerValue ?: "")
                    if(validationResult != null) {
                        failList.add(VerifyHeadersError.InvalidType(expectedHeader.name, expectedHeader.type.toString(), convertToType(headerValue).toString()))
                    }
                }
                is ContentOrSchema.ContentField -> {
                    val contentField = headerType.content[contentType]
                    if(contentField == null) {
                        failList.add(VerifyHeadersError.InvalidType(expectedHeader.name, expectedHeader.type.toString(), convertToType(headerValue).toString()))
                    }
                    val validationResult = jsonValidator(contentField?.schema, headerValue ?: "")
                    if(validationResult != null) {
                        failList.add(VerifyHeadersError.InvalidType(expectedHeader.name, expectedHeader.type.toString(), convertToType(headerValue).toString()))
                    }
                }
            }
        }
        if(headers["Content-Type"] != null) {
            if(headers["Content-Type"] != contentType) {
                failList.add(VerifyHeadersError.InvalidContentType(contentType, headers["Content-Type"] ?: ""))
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

        val receivedBody = JsonParser("\"$receivedType\"").parse()
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
            //TODO
            expectedBody.content.content.forEach { k, v ->
                if (k == contentType) {
                    val schema = SchemaLoader(v.schema).load()
                    val validator = Validator.create(schema, ValidatorConfig(FormatValidationPolicy.ALWAYS))

                    val receivedBody = JsonParser(body).parse()
                    val validationResult = validator.validate(receivedBody)

                    if(validationResult != null) {
                        failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.schema.toString()))
                    }
                }
            }

        } catch (e: Exception) {
            failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.schema.toString()))
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
