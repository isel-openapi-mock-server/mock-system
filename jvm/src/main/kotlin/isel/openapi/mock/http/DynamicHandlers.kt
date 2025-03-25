package isel.openapi.mock.http

import isel.openapi.mock.utils.ApiParameter
import isel.openapi.mock.utils.ApiRequestBody
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

interface DynamicHandler {

}

//Sem parametros e sem body
class BasicDynamicHandler(private val response: String): DynamicHandler {
    @ResponseBody
    fun handle(): String {
        return response
    }
}

//Com parametros e sem body
class ParamsDynamicHandler(private val response: String, private val params: List<ApiParameter>): DynamicHandler {
    @ResponseBody
    fun handle(
        @RequestParam requestParams: Map<String, String>,
    ): String {
        if(requestParams.keys.any { key -> params.none { it.name == key } }) {
            return "Response: $response | Params: $requestParams | Error: Invalid parameters"
        }
        if(params.filter { it.required }.any { it.name !in requestParams.keys }) {
            return "Response: $response | Params: $requestParams | Error: Missing required parameters"
        }
        return "Response: $response | Params: $requestParams"
    }
}

//Sem parametros e com body
class BodyDynamicHandler(private val response: String, private val body: ApiRequestBody): DynamicHandler {
    @ResponseBody
    fun handle(
        request: HttpServletRequest,
    ): String {
        val requestBody = request.reader.readText()
        val bodyMap = convertJsonToMap(requestBody)
        if(bodyMap.keys != body.parameters.keys) {
            return "Response: $response | Body: $requestBody | Error: Invalid body"
        }
        return "Response: $response | Body: $requestBody"
    }

    private fun convertJsonToMap(jsonString: String): Map<String, Any> {
        val objectMapper = jacksonObjectMapper()
        return objectMapper.readValue(jsonString)
    }
}

