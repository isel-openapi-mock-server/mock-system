package isel.openapi.mock.utils

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser

fun validateOpenApi(json: String): Boolean {
    val result = OpenAPIV3Parser().readContents(json)
    println(result)
    return result.messages.isEmpty() // Se estiver vazio, a definição é válida.
}

fun parseOpenApi(json: String): OpenAPI? {
    return OpenAPIV3Parser().readContents(json)?.openAPI
}

fun extractApiSpec(openAPI: OpenAPI): ApiSpec {
    return ApiSpec(
        paths = openAPI.paths.map { (path, pathItem) ->
            ApiPath(
                path = path,
                methods = pathItem.readOperationsMap().map { (method, operation) ->
                    ApiMethod(
                        method = method.name,
                        security = operation.security ?: emptyList(),
                        parameters = operation.parameters?.map { param ->
                            ApiParameter(
                                name = param.name,
                                type = param.schema?.type ?: "unknown",
                                required = param.required ?: false,
                                allowEmptyValue = param.allowEmptyValue ?: false,
                                location = param.`in`,
                                style = param.style.toString().ifEmpty { "form" },
                                explode = param.explode ?: false,
                            )
                        } ?: emptyList(),
                        requestBody = operation.requestBody?.let { reqBody ->
                            val mediaType = reqBody.content?.keys?.firstOrNull() ?: "unknown"
                            val schemaType = reqBody.content?.get(mediaType)?.schema?.type
                            ApiRequestBody(
                                contentType = mediaType,
                                schemaType = schemaType,
                                required = reqBody.required ?: false
                            )
                        },
                        responses = operation.responses.map { (statusCode, response) ->
                            val contentType = response.content?.keys?.firstOrNull()
                            ApiResponse(
                                statusCode = statusCode,
                                contentType = contentType
                            )
                        }
                    )
                }
            )
        }
    )
}