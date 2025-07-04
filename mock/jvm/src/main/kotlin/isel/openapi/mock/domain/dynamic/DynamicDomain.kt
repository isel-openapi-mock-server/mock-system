package isel.openapi.mock.domain.dynamic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.erosb.jsonsKema.*
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import io.swagger.v3.oas.models.media.Schema
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

    private val factory: JsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)

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
                if(validationResult.isNotEmpty() && validationResult.any { !isHandleBars(it) }) {
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
                if(validationResult.isNotEmpty() && validationResult.any { !isHandleBars(it) }) {
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
                val validationResult = jsonValidator(schema, cookie.value)
                if (validationResult.isNotEmpty() && validationResult.any { !isHandleBars(it) }) {
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
                    if(validationResult.isNotEmpty() && validationResult.any { !isHandleBars(it) }) {
                        failList.add(VerifyHeadersError.InvalidType(expectedHeader.name, expectedHeader.type.toString(), convertToType(headerValue).toString()))
                    }
                }
                is ContentOrSchema.ContentField -> {
                    val contentField = headerType.content[contentType]
                    if(contentField == null) {
                        failList.add(VerifyHeadersError.InvalidType(expectedHeader.name, expectedHeader.type.toString(), convertToType(headerValue).toString()))
                    }
                    val validationResult = jsonValidator(contentField?.schema, "\"$headerValue\"")
                    if(validationResult.isNotEmpty() && validationResult.any { !isHandleBars(it) }) {
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
                    if(validationResult.isNotEmpty() && validationResult.any { !isHandleBars(it) }) {
                        failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.content.content[contentType]?.schema.toString() ?: contentType, body))
                    } else if(contentType == "application/json") {
                        val objectMapper = ObjectMapper()
                        val schemaObj = objectMapper.readValue(value.schema, Schema::class.java)

                        if (schemaObj.type == "object") {
                            val jsonNode = objectMapper.readTree(body)
                            if (!jsonNode.isObject) {
                                failList.add(
                                    VerifyBodyError.InvalidBodyFormat(
                                        schemaObj.toString(),
                                        body
                                    )
                                )
                            } else {
                                val bodyMap = objectMapper.convertValue(jsonNode, Map::class.java) as Map<String, Any?>
                                val allFields = schemaObj.properties?.keys ?: emptySet()
                                val extraFields = bodyMap.keys.filter { it !in allFields }
                                val requiredFields = schemaObj.required ?: emptyList()
                                val missingFields = requiredFields.filter { it !in bodyMap.keys }
                                if (missingFields.isNotEmpty()) {
                                    missingFields.forEach {
                                        failList.add(
                                            VerifyBodyError.InvalidBodyFormat(
                                                "Missing required field: $it in body",
                                                body
                                            )
                                        )
                                    }
                                }
                                if (extraFields.isNotEmpty()) {
                                    extraFields.forEach {
                                        failList.add(
                                            VerifyBodyError.InvalidBodyFormat(
                                                "Extra field found: $it in body",
                                                body
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Erro no JSON do schema: ${e.message}")
            failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.content.content[contentType]?.schema.toString() ?: contentType, body))
        }
        return failList
    }

    private fun jsonValidator(
        schema: String?,
        receivedType: String,
    ): Set<ValidationMessage> {

        if(schema == null) { return emptySet() }

        val jsonSchema: JsonSchema = factory.getSchema(schema)

        val mapper = ObjectMapper()
        try {
            val jsonNode: JsonNode = mapper.readTree(receivedType)

            val errors: Set<ValidationMessage> = jsonSchema.validate(jsonNode)

            return errors
        } catch (e: JsonParseException) {
            return setOf(
                ValidationMessage.builder()
                    .message("Invalid JSON format: ${e.message}")
                    .build()
            )
        }
    }

    fun verifyResponseBody(
        body: ByteArray?,
        contentType: String,
        bodySpec: ContentOrSchema.ContentField?
    ): List<VerificationError> {

        if (body == null) return emptyList()

        val bodyString = String(body, Charsets.UTF_8)

        val failList = mutableListOf<VerificationError>()

        if (bodySpec == null) return failList

        try {
            bodySpec.content.forEach { (key, value) ->
                if (key == contentType) {
                    val validationResult = jsonValidator(value.schema, bodyString)
                    if (validationResult != null && validationResult.isNotEmpty()) {
                        for (message in validationResult) {
                            failList.add(
                                VerifyBodyError.InvalidResponseBodyFormat(
                                        bodySpec.content[contentType]?.schema.toString(), bodyString
                                    )
                                )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            failList.add(
                VerifyBodyError.InvalidResponseBodyFormat(
                    bodySpec.content[contentType]?.schema.toString(), bodyString
                )
            )
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

    private fun isHandleBars(message: ValidationMessage): Boolean {

        val node = message.instanceNode?.asText() ?: return false
        val size = node.length

        return node.first() == '{' && node[1] == '{' && node.last() == '}' && node[size - 2] == '}'

    }

}