package isel.openapi.mock.http

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.ResponseBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import isel.openapi.mock.parsingServices.model.*
import jakarta.servlet.http.Cookie
import org.springframework.http.ResponseEntity

// TODO Fazer com que todas as sealed classes/intefaces implementem esta.
interface VerificationError

sealed class VerifyBodyError: VerificationError {
    data class InvalidBodyTypes(val name: String?, val expectedType: Type, val receivedType: Type): VerifyBodyError()
    data class InvalidBodyKeys(val expectedKeys: String, val receivedKeys: String): VerifyBodyError()
    data class InvalidBodyFormat(val expectedBodyType: Type): VerifyBodyError()
    data class InvalidArrayElement(val expectedType: Type, val receivedType: Type): VerifyBodyError()
}

sealed class VerifyHeadersError: VerificationError {
    data class InvalidType(val headerKey: String, val expectedType: Type, val receivedType: Type): VerifyHeadersError()
    data class InvalidHeader(val unexpectedHeaders: Set<String>): VerifyHeadersError()
    data class InvalidContentType(val expectedType: String, val receivedType: String): VerifyHeadersError()
    data class MissingHeader(val expectedHeaders: String): VerifyHeadersError()
    data class MissingHeaderContent(val headerKey: String): VerifyHeadersError()
}

// TODO separar nos tipos de params diferentes (query, path e cookies).
sealed class VerifyParamsError : VerificationError {
    data class InvalidType(val location: Location, val expectedType: Type, val receivedType: Type) : VerifyParamsError()
    data class ParamCantBeEmpty(val location: Location, val paramName: String) : VerifyParamsError()
    data class InvalidParam(val location: Location, val paramName: String) : VerifyParamsError()
    data class MissingParam(val location: Location, val paramName: String) : VerifyParamsError()
}

class DynamicHandler(
    private val path: List<PathParts>,
    private val response: String,
    private val params: List<ApiParameter>?,
    private val body: ApiRequestBody?
) {

    fun handle(
        request: HttpServletRequest,
    ): ResponseEntity<*> {
        val requestBody = request.reader.readText()
        val requestQueryParams = request.parameterMap.mapValues { entry -> entry.value.map { value -> value.toTypedValue() } } // Mudei de it.value[0], para it.value
        val requestPathParams = getPathParams(request.requestURI)
        val headers = request.headerNames.toList().associate { it to request.getHeader(it) }
        val cookies = request.cookies

        val fails = mutableListOf<VerificationError>()

        if(body != null) {
            val bodyResult = verifyBody(requestBody, body)
            bodyResult.forEach { fails.add(it) }
        }

        val headersResult = verifyHeaders(headers, params?.filter { it.location == Location.HEADER } ?: emptyList(), body?.contentType ?: "")
        headersResult.forEach { fails.add(it) }

        val queryParamsResult = verifyQueryParams(requestQueryParams, params?.filter { it.location == Location.QUERY } ?: emptyList())
        queryParamsResult.forEach { fails.add(it) }

        val pathParamsResult = verifyPathParams(requestPathParams, params?.filter { it.location == Location.PATH } ?: emptyList())
        pathParamsResult.forEach { fails.add(it) }

        val cookiesResult = verifyCookies(cookies, params?.filter { it.location == Location.COOKIE } ?: emptyList())
        cookiesResult.forEach { fails.add(it) }

        return if(fails.isNotEmpty()) {
            //TODO: Converter os erros para um formato mais legível.
            ResponseEntity.badRequest().body(fails)
        } else  ResponseEntity.ok(response)
    }

    private fun convertJsonToMap(jsonString: String): Map<String, Any> {
        val objectMapper = jacksonObjectMapper()
        return objectMapper.readValue<Map<String, Any>>(jsonString)
    }

    private fun convertStringToArray(arrayString: String): List<Any> {
        val objectMapper = jacksonObjectMapper()
        return objectMapper.readValue<List<Any>>(arrayString)
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
                    failList.add(VerifyParamsError.InvalidType(Location.QUERY, param.type, valueType))
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
                failList.add(VerifyParamsError.InvalidType(Location.PATH, param.type, valueType))
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
        expectedHeaders: List<ApiParameter>,
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
            if(headerValue != null && convertToType(headerValue) != expectedHeader.type) {
                failList.add(VerifyHeadersError.InvalidType(expectedHeader.name, expectedHeader.type, convertToType(headerValue)))
            }
        }
        if(headers["Content-Type"] != contentType) {
            failList.add(VerifyHeadersError.InvalidContentType(contentType, headers["Content-Type"] ?: ""))
        }
        val unexpectedHeaders = headers.keys - expectedHeaders.map { it.name }
        if(unexpectedHeaders.isNotEmpty()) {
            failList.add(VerifyHeadersError.InvalidHeader(unexpectedHeaders))
        }
        return failList
    }

    fun verifyBody(
        body: String,
        expectedBody: ApiRequestBody
    ): List<VerifyBodyError> {

        val failList = mutableListOf<VerifyBodyError>()

        when(expectedBody.schemaType) {
            is Type.NullType -> {
                if(body.isNotEmpty())
                    failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.schemaType))
            }
            is Type.BooleanType -> {
                if(body != "true" && body != "false")
                    failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.schemaType))
            }
            is Type.IntegerType -> {
                if(body.toIntOrNull() == null)
                    failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.schemaType))
            }
            is Type.NumberType -> {
                if(body.toDoubleOrNull() == null)
                    failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.schemaType))
            }
            is Type.StringType -> {
                if(body.isEmpty())
                    failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.schemaType))
            }
            is Type.ArrayType -> {
                try {
                    val array = convertStringToArray(body)
                    val elementType = expectedBody.schemaType.elementsType
                    array.forEach { element ->
                        if(elementType != convertToType(element)) {
                            failList.add(VerifyBodyError.InvalidArrayElement(elementType, convertToType(element)))
                        }
                    }
                } catch (e: Exception) {
                    failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.schemaType))
                }
            }
            is Type.ObjectType -> {
                try {
                    //Converte o body recebido para um map<string,any>
                    val requestBodyMap = convertJsonToMap(body)
                    //Body experado extraido da definição openAPI e armazenado num map<string,Type>
                    val expectedBodyMap = expectedBody.schemaType.fieldsTypes
                    if(requestBodyMap.keys != expectedBodyMap.keys) {
                        failList.add(VerifyBodyError.InvalidBodyKeys(expectedBodyMap.keys.toString(), requestBodyMap.keys.toString()))
                    }
                    //Converte o body recebido para um map<string,Type>
                    val requestBodyTypes = requestBodyMap.mapValues { convertToType(it.value) }
                    //Verifica se os tipos do body recebido correspondem aos tipos esperados
                    requestBodyTypes.forEach { (key, type) ->
                        if(type != expectedBodyMap[key]!!) {
                            failList.add(VerifyBodyError.InvalidBodyTypes(key, expectedBodyMap[key]!!, type))
                        }
                    }
                    //Se o body não for um json a função convertJsonToMap lança uma exceção
                } catch (e: Exception) {
                    failList.add(VerifyBodyError.InvalidBodyFormat(expectedBody.schemaType))
                }
            }
            Type.UnknownType -> { }
        }

        return failList

    }

    fun convertToType(value: Any?): Type {
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
