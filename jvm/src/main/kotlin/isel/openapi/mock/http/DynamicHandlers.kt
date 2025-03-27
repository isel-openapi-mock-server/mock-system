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
import isel.openapi.mock.parsingServices.model.Type

interface DynamicHandler {

}

class BodyAndParamsDynamicHandler(
    private val response: String,
    private val params: List<ApiParameter>?,
    private val body: ApiRequestBody?
): DynamicHandler {
    @ResponseBody
    fun handle(
        request: HttpServletRequest,
    ): String {
        val requestBody = request.reader.readText()
        val requestParams = request.parameterMap.mapValues { it.value[0] }

        TODO()

        /*
        if(params != null) {
            if(requestParams.keys.any { key -> params.none { it.name == key } }) {
                return "Response: $response | Params: $requestParams | Error: Invalid parameters"
            }
            if(params.filter { it.required }.any { it.name !in requestParams.keys }) {
                return "Response: $response | Params: $requestParams | Error: Missing required parameters"
            }
        }

        if (body != null) {
            val bodyMap = if(!requestBody.isEmpty()) convertJsonToMap(requestBody) else emptyMap()
            if(bodyMap.keys != body.parameters.keys) {
                return "Response: $response | Body: $requestBody | Error: Invalid body"
            }
        }

         */

        return "Response: $response | Body: $requestBody | Params: $requestParams"
    }

    private fun convertJsonToMap(jsonString: String): Map<String, Any> {
        val objectMapper = jacksonObjectMapper()
        return objectMapper.readValue(jsonString)
    }

    fun getPathParams() {
        TODO()
    }

    fun verifyParams() {
        TODO()
    }

    fun verifyBody(body: String, expectedBody: ApiRequestBody): Boolean {

        if(expectedBody.schemaType is Type.ObjectType) {
            try {
                //Converte o body recebido para um map<string,any>
                val requestBodyMap = convertJsonToMap(body)

                //Body experado extraido da definição openAPI e armazenado num map<string,Type>
                val expectedBodyMap = expectedBody.schemaType.fieldsTypes
                if(requestBodyMap.keys != expectedBodyMap.keys) {
                    throw IllegalArgumentException("Invalid body - keys")
                }

                //Converte o body recebido para um map<string,Type>
                val requestBodyTypes = requestBodyMap.mapValues { convertToType(it.value) }

                //Verifica se os tipos do body recebido correspondem aos tipos esperados
                requestBodyTypes.forEach { (key, type) ->
                    if(type != expectedBodyMap[key]!!) {
                        throw IllegalArgumentException("Invalid body - types")
                    }
                }

                return true

                //Se o body não for um json a função convertJsonToMap lança uma exceção
            } catch (e: Exception) {
                println(e.message)
                throw IllegalArgumentException("Invalid body - json")
            }
        }
        else {
            throw IllegalArgumentException("Invalid body")
        }

        return false

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

