package isel.openapi.mock.utils

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
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
                            val schema = reqBody.content?.get(mediaType)?.schema
                            ApiRequestBody(
                                contentType = mediaType,
                                schemaType = schema?.type ?: "unknown",
                                required = reqBody.required ?: false,
                                parameters = schema?.let { extractSchemaProperties(it) } ?: emptyMap()
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

fun extractSchemaProperties(schema: Schema<*>): Map<String, Any> {
    val properties = mutableMapOf<String, Any>()

    if (schema is ObjectSchema && schema.properties != null) {
        schema.properties.forEach { (name, propertySchema) ->
            properties[name] = if (propertySchema is ObjectSchema) {
                extractSchemaProperties(propertySchema) // Se for um objeto, extrai recursivamente
            } else {
                propertySchema.type ?: "unknown"
            }
        }
    }
    return properties
}