package isel.openapi.mock.domain.dynamic

import com.github.erosb.jsonsKema.*
import isel.openapi.mock.domain.openAPI.*
import isel.openapi.mock.domain.problems.ParameterInfo
import isel.openapi.mock.http.VerificationError
import isel.openapi.mock.http.VerifyBodyError
import isel.openapi.mock.http.VerifyHeadersError
import isel.openapi.mock.http.VerifyParamsError
import jakarta.servlet.http.Cookie
import org.springframework.stereotype.Component

class VerifyParamsResult(
    val params: List<ParameterInfo>,
    val errors: List<VerifyParamsError>
)

//TODO: meti aqui porque tava a precisar nos services (talvez voltar a meter onde tava ou separar em classes)
@Component
class DynamicDomain {

    fun getPathParams(
        path: List<PathParts>,
        uri: String
    ): Map<String, Any> {
        val parts = uri.split("/").filter{ it.isNotBlank() }.ifEmpty { return emptyMap() }
        val pathParams = mutableMapOf<String, Any>()
        path.forEachIndexed{ idx, part ->
            if (part.isParam) {
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

    fun verifyQueryParams(
        queryParams: Map<String, List<Any>>,
        expectedQueryParams: List<ApiParameter>
    ): VerifyParamsResult {

        val failList = mutableListOf<VerifyParamsError>()

        val params = mutableListOf<ParameterInfo>()

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

        if(failList.isNotEmpty()) return VerifyParamsResult(params, failList)

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
                        VerifyParamsError.InvalidType(expectedParam.name, expectedParam.location, expectedParam.type.toString(), convertToType(value).toString())
                    )?.let { failList.add(it as VerifyParamsError) }

                    params.add(
                        ParameterInfo(
                            expectedParam.name,
                            value.toString(),
                            expectedParam.location,
                            expectedParam.type
                        )
                    )

                }
            }
        }
        return VerifyParamsResult(params, failList)
    }

    fun validateContentOrSchema(
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

    fun verifyPathParams(
        pathParams: Map<String, Any>,
        expectedPathParams: List<ApiParameter>
    ): VerifyParamsResult {

        val failList = mutableListOf<VerifyParamsError>()
        val params = mutableListOf<ParameterInfo>()

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

        if (failList.isNotEmpty()) return VerifyParamsResult(params, failList)

        expectedPathParams.forEach { expectedParam ->
            if(expectedParam.required && !pathParams.containsKey(expectedParam.name)) {
                failList.add(VerifyParamsError.MissingParam(expectedParam.location,  expectedParam.name))
            }

            val paramValue = pathParams[expectedParam.name]

            // Desnecessário
            if(paramValue == null && expectedParam.required) {
                failList.add(VerifyParamsError.MissingParam(expectedParam.location,  expectedParam.name))
            }

            val type = convertToType(paramValue)

            validateContentOrSchema(
                expectedParam.type,
                type,
                paramValue ?: "",
                VerifyParamsError.InvalidType(expectedParam.name, expectedParam.location, expectedParam.type.toString(), convertToType(paramValue).toString())
            )?.let { failList.add(it as VerifyParamsError) }

            params.add(
                ParameterInfo(
                    expectedParam.name,
                    pathParams[expectedParam.name].toString(),
                    expectedParam.location,
                    expectedParam.type
                )
            )

        }
        return VerifyParamsResult(params, failList)
    }

    fun verifyCookies(
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

            var schema: String? = null

            if (type is ContentOrSchema.ContentField) {
                schema = type.content.entries.first().value.schema //Apenas uma entrada no content para parametros.
            } else if (type is ContentOrSchema.SchemaObject) {
                schema = type.schema
            }

            if (cookie.value.isBlank()) {
                if (!expCookie.allowEmptyValue) {
                    failList.add(VerifyParamsError.ParamCantBeEmpty(location = Location.COOKIE, cookie.name))
                }
            } else {
                val validationResult = jsonValidator(schema, cookie.value) //TODO temos de fazer algo com isto, acrescentar uma falha para se vier algum erro daqui e adicionar à lista.
                if (validationResult != null) {
                    failList.add(VerifyParamsError.JsonValidationError(location = Location.COOKIE))
                }
            }

        }

        cookies.forEach { cookie ->
            if (cookie.name !in expectedCookies.map { it.name }) {
                failList.add(VerifyParamsError.InvalidParam(location = Location.COOKIE, paramName = cookie.name))
            }

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
        if(contentType != null && headers["content-type"] != null) {
            if(headers["content-type"] != contentType) {
                failList.add(VerifyHeadersError.InvalidContentType(contentType, headers["content-type"] ?: ""))
            }
        }
        if(security && headers["authorization"] == null) {
            failList.add(VerifyHeadersError.MissingHeader("Authorization"))
        }
        if(security && headers["authorization"] != null) {
            val authHeader = headers["authorization"] ?: ""
            if(!authHeader.startsWith("Bearer ")) {
                failList.add(VerifyHeadersError.InvalidType("Authorization", "Bearer Token", authHeader))
            } else if(authHeader.substringAfter("Bearer ").trim().length <= 30) {
                failList.add(VerifyHeadersError.InvalidType("Authorization", "Bearer Token", authHeader))
            }
        }
        return failList
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
                        failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.content.content[contentType]?.schema.toString() ?: contentType, body))
                    }
                }
            }
        } catch (e: Exception) {
            failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.content.content[contentType]?.schema.toString() ?: contentType, body))
        }
        return failList
    }

    private fun jsonValidator(
        schema: String?,
        receivedType: String,
    ): ValidationFailure? {

        if(schema == null) { return null }

        val jsonVal = JsonParser(schema).parse()

        val schemaLoader = SchemaLoader(jsonVal)
        val validator = Validator.create(schemaLoader.load(), ValidatorConfig(FormatValidationPolicy.ALWAYS))
        try {
            val receivedJsonType = JsonParser(receivedType).parse()
            val validationResult = validator.validate(receivedJsonType)
            return validationResult
        } catch (e: JsonParseException) {
            println(e.location)
            println(e.message)
            println(e.localizedMessage)
            println(e.cause)
            println(e.suppressed)
            println(e.stackTrace)
            return null // TODO mudar
        }
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