package isel.openapi.mock.http

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.ResponseBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import isel.openapi.mock.parsingServices.model.*
import isel.openapi.mock.utils.Either
import isel.openapi.mock.utils.Failure
import isel.openapi.mock.utils.failure
import isel.openapi.mock.utils.success
import jakarta.servlet.http.Cookie
import org.springframework.http.ResponseEntity

sealed class VerifyBodyError {
    data object InvalidBodyTypes: VerifyBodyError()
    data object InvalidBodyKeys: VerifyBodyError()
    data object InvalidBodyFormat: VerifyBodyError()
}

typealias VerifyBodyResult = Either<VerifyBodyError, Boolean>

sealed class VerifyHeadersError {
    data object InvalidType: VerifyHeadersError()
    data object InvalidHeader: VerifyHeadersError()
    data object InvalidContentType: VerifyHeadersError()
    data object InvalidHeaderFormat: VerifyHeadersError()
    data object MissingHeader: VerifyHeadersError()
}

typealias VerifyHeadersResult = Either<VerifyHeadersError, Boolean>

sealed class VerifyParamsError {

}

typealias VerifyParamsResult = Either<VerifyParamsError, Boolean>

class BodyAndParamsDynamicHandler(
    private val path: List<PathParts>,
    private val response: String,
    private val params: List<ApiParameter>?,
    private val body: ApiRequestBody?
) {
    @ResponseBody
    fun handle(
        request: HttpServletRequest,
    ): ResponseEntity<*> {
        val requestBody = request.reader.readText()
        val requestQueryParams = request.parameterMap.mapValues { entry -> entry.value.map { value -> value.toTypedValue() } } // Mudei de it.value[0], para it.value
        val requestPathParams = getPathParams(request.requestURI)
        val headers = request.headerNames.toList().associate { it to request.getHeader(it) }
        val cookies = request.cookies

        if(body != null) {
            val bodyResult = verifyBody(requestBody, body)
            if(bodyResult is Failure) {
                return when(bodyResult.value) {
                    is VerifyBodyError.InvalidBodyTypes -> ResponseEntity.badRequest().body("Invalid body types")
                    is VerifyBodyError.InvalidBodyKeys -> ResponseEntity.badRequest().body("Invalid body keys")
                    is VerifyBodyError.InvalidBodyFormat -> ResponseEntity.badRequest().body("Invalid body format")
                }
            }
        }

        val headersResult = verifyHeaders(headers, params?.filter { it.location == Location.HEADER } ?: emptyList(), body?.contentType ?: "")
        if(headersResult is Failure) {
            return when(headersResult.value) {
                is VerifyHeadersError.InvalidType -> ResponseEntity.badRequest().body("Invalid header type")
                is VerifyHeadersError.InvalidHeader -> ResponseEntity.badRequest().body("Invalid header")
                is VerifyHeadersError.InvalidContentType -> ResponseEntity.badRequest().body("Invalid content type")
                is VerifyHeadersError.InvalidHeaderFormat -> ResponseEntity.badRequest().body("Invalid header format")
                is VerifyHeadersError.MissingHeader -> ResponseEntity.badRequest().body("Missing header")
            }
        }

        val paramsResult = verifyParams(requestQueryParams, requestPathParams, cookies, params?.filter { it.location != Location.HEADER } ?: emptyList())
        if (paramsResult is Failure) {
            TODO()
        }

        return ResponseEntity.ok(response)
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

    private fun verifyParams(
        queryParams: Map<String, List<Any>>,
        pathParams: Map<String, Any>,
        cookies: Array<Cookie>,
        expectedParams: List<ApiParameter>,
    ): VerifyParamsResult {
        val expectedQueryParams = expectedParams.filter { it.location == Location.QUERY }
        val expectedPathParams = expectedParams.filter { it.location == Location.PATH }
        val expectedCookies = expectedParams.filter { it.location == Location.COOKIE }

        var failed = false
        val failList = mutableListOf<VerifyParamsError>()

        if (
            expectedQueryParams.isNotEmpty() && queryParams.isEmpty() ||
            expectedQueryParams.isEmpty() && queryParams.isNotEmpty()
        ) {
            failed = true
            // TODO adicionar erro à failList, para depois guardarmos os erros.
        }

        if (
            expectedPathParams.isNotEmpty() && pathParams.isEmpty() ||
            expectedPathParams.isEmpty() && pathParams.isNotEmpty()
        ) {
            failed = true
            // TODO adicionar erro à failList, para depois guardarmos os erros.
        }

        if (
            expectedCookies.isNotEmpty() && cookies.isEmpty() ||
            expectedCookies.isEmpty() && cookies.isNotEmpty()
        ) {
            failed = true
            // TODO adicionar erro à failList, para depois guardarmos os erros.
        }


        if (!failed && expectedQueryParams.isNotEmpty()) { // Nao sei se nao vemos se já tiver falhado, acho que nao
            verifyQueryParams(queryParams, expectedQueryParams) // TODO Guardar os erros
        }

        if (!failed && expectedPathParams.isNotEmpty()) { // Nao sei se nao vemos se já tiver falhado, acho que nao
            verifyPathParams(pathParams, expectedPathParams)// TODO guadar od ersos
        }

        if (!failed && expectedCookies.isNotEmpty()) { // Nao sei se nao vemos se já tiver falhado, acho que nao
            verifyCookies(cookies, expectedCookies)// TODO guadar od ersos
        }

        TODO()
    }


    private fun verifyQueryParams(
        queryParams: Map<String, List<Any>>,
        expectedQueryParams: List<ApiParameter>
    ): VerifyParamsResult {

        expectedQueryParams.forEach { param ->
            val values = queryParams[param.name]
                ?: if (param.required) {
                    TODO() // erro
                }
                else
                    return@forEach
            for (value in values) {
                val valueType = convertToType(value)
                // Verificar se nao tem valor, String vazia, e o param não o suporta.
                if (
                    valueType == Type.StringType &&
                    (value as String).isBlank()
                ) {
                    if (!param.allowEmptyValue) {
                        // TODO erro, o valor do param é vazio mas o param nao pode vir vazio
                    }
                    else {
                        // O valor pode vir vazio, avança para o proximo ciclo do forEach.
                        return@forEach
                    }

                }
                if (valueType == param.type) {
                    // TODO Sem problemas
                }
            }

        }
        TODO()
    }

    private fun verifyPathParams(
        pathParams: Map<String, Any>,
        expectedPathParams: List<ApiParameter>
    ): VerifyParamsResult {

        expectedPathParams.forEach { param ->
            val value = pathParams[param.name]
                ?: if (param.required) {
                    TODO() // erro
                }
                else
                    return@forEach

            val valueType = convertToType(value)
            // Verificar se nao tem valor, String vazia, e o param não o suporta.
            if (
                valueType == Type.StringType &&
                (value as String).isBlank()
            ) {
                if (!param.allowEmptyValue) {
                    // TODO erro, o valor do param é vazio mas o param nao pode vir vazio
                }
                else {
                    // O valor pode vir vazio, avança para o proximo ciclo do forEach.
                    return@forEach
                }

            }
            if (valueType == param.type) {
                // TODO Sem problemas
            }
        }
        TODO()
    }

    private fun verifyCookies(
        cookies: Array<Cookie>,
        expectedCookies: List<ApiParameter>,
    ): VerifyParamsResult {
        TODO()
    }

    fun verifyHeaders(
        headers: Map<String, String>,
        expectedHeaders: List<ApiParameter>,
        contentType: String
    ): VerifyHeadersResult {
        expectedHeaders.forEach { expectedHeader ->
            if(expectedHeader.required && !headers.containsKey(expectedHeader.name)) {
                return failure(VerifyHeadersError.MissingHeader)
            }
            val headerValue = headers[expectedHeader.name]
            if(headerValue == null && expectedHeader.required) {
                return failure(VerifyHeadersError.MissingHeader)
            }
            if(headerValue != null && convertToType(headerValue) != expectedHeader.type) {
                return failure(VerifyHeadersError.InvalidType)
            }
        }
        if(headers["Content-Type"] != contentType) {
            return failure(VerifyHeadersError.InvalidContentType)
        }
        val unexpectedHeaders = headers.keys - expectedHeaders.map { it.name }
        if(unexpectedHeaders.isNotEmpty()) {
            return failure(VerifyHeadersError.InvalidHeader)
        }
        return success(true)
    }

    fun verifyBody(
        body: String,
        expectedBody: ApiRequestBody
    ): VerifyBodyResult {
        return when(expectedBody.schemaType) {
            is Type.NullType -> if(body.isEmpty()) success(true) else failure(VerifyBodyError.InvalidBodyTypes)
            is Type.BooleanType -> if(body == "true" || body == "false") success(true) else failure(VerifyBodyError.InvalidBodyTypes)
            is Type.IntegerType -> if(body.toIntOrNull() != null) success(true) else failure(VerifyBodyError.InvalidBodyTypes)
            is Type.NumberType -> if(body.toDoubleOrNull() != null) success(true) else failure(VerifyBodyError.InvalidBodyTypes)
            is Type.StringType -> if(body.isNotEmpty()) success(true) else failure(VerifyBodyError.InvalidBodyTypes)
            is Type.ArrayType -> {
                try {
                    val array = convertStringToArray(body)
                    val elementType = expectedBody.schemaType.elementsType
                    array.forEach { element ->
                        if(elementType != convertToType(element)) {
                            return failure(VerifyBodyError.InvalidBodyTypes)
                        }
                    }
                    success(true)
                } catch (e: Exception) {
                    return failure(VerifyBodyError.InvalidBodyFormat)
                }
            }
            is Type.ObjectType -> {
                try {
                    //Converte o body recebido para um map<string,any>
                    val requestBodyMap = convertJsonToMap(body)
                    //Body experado extraido da definição openAPI e armazenado num map<string,Type>
                    val expectedBodyMap = expectedBody.schemaType.fieldsTypes
                    if(requestBodyMap.keys != expectedBodyMap.keys) {
                        return failure(VerifyBodyError.InvalidBodyKeys)
                    }
                    //Converte o body recebido para um map<string,Type>
                    val requestBodyTypes = requestBodyMap.mapValues { convertToType(it.value) }
                    //Verifica se os tipos do body recebido correspondem aos tipos esperados
                    requestBodyTypes.forEach { (key, type) ->
                        if(type != expectedBodyMap[key]!!) {
                            return failure(VerifyBodyError.InvalidBodyTypes)
                        }
                    }
                    success(true)
                    //Se o body não for um json a função convertJsonToMap lança uma exceção
                } catch (e: Exception) {
                    return failure(VerifyBodyError.InvalidBodyFormat)
                }
            }
            Type.UnknownType -> success(true)
        }
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
