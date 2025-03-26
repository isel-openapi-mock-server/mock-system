package isel.openapi.mock.http

import isel.openapi.mock.parsingServices.model.ApiParameter
import isel.openapi.mock.parsingServices.model.ApiRequestBody
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.ResponseBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

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
}

