package isel.openapi.mock.http

import isel.openapi.mock.parsingServices.model.ApiParameter
import isel.openapi.mock.parsingServices.model.ApiRequestBody
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.ResponseBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.StringSchema
import isel.openapi.mock.parsingServices.model.Location
import isel.openapi.mock.parsingServices.model.Type
import isel.openapi.mock.utils.Either
import isel.openapi.mock.utils.Failure
import isel.openapi.mock.utils.Success
import isel.openapi.mock.utils.failure
import isel.openapi.mock.utils.success
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

class BodyAndParamsDynamicHandler(
    private val response: String,
    private val params: List<ApiParameter>?,
    private val body: ApiRequestBody?
) {
    @ResponseBody
    fun handle(
        request: HttpServletRequest,
    ): ResponseEntity<*> {
        val requestBody = request.reader.readText()
        val requestParams = request.parameterMap.mapValues { it.value[0] }
        val headers = request.headerNames.toList().associate { it to request.getHeader(it) }

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

    fun getPathParams() {
        TODO()
    }

    fun verifyParams() {
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
