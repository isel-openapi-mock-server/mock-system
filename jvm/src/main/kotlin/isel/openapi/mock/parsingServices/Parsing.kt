package isel.openapi.mock.parsingServices

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.parser.OpenAPIV3Parser
import isel.openapi.mock.parsingServices.model.ApiParameter
import isel.openapi.mock.parsingServices.model.ApiPath
import isel.openapi.mock.parsingServices.model.ApiRequestBody
import isel.openapi.mock.parsingServices.model.ApiResponse
import isel.openapi.mock.parsingServices.model.ApiServer
import isel.openapi.mock.parsingServices.model.ApiSpec
import isel.openapi.mock.parsingServices.model.HttpMethod
import isel.openapi.mock.parsingServices.model.Location
import isel.openapi.mock.parsingServices.model.ParameterStyle
import isel.openapi.mock.parsingServices.model.PathOperation
import isel.openapi.mock.parsingServices.model.PathParts
import isel.openapi.mock.parsingServices.model.ServerVariable
import isel.openapi.mock.parsingServices.model.StatusCode
import isel.openapi.mock.parsingServices.model.StatusCode.Companion.fromCode
import isel.openapi.mock.parsingServices.model.Type
import isel.openapi.mock.parsingServices.model.Type.ArrayType
import isel.openapi.mock.parsingServices.model.Type.ObjectType
import org.springframework.stereotype.Component

@Component
class Parsing {

    fun validateOpenApi(json: String): Boolean {
        val result = OpenAPIV3Parser().readContents(json)
        return result.messages.isEmpty() // Se estiver vazio, a definição é válida.
    }

    fun parseOpenApi(json: String): OpenAPI? {
        return OpenAPIV3Parser().readContents(json)?.openAPI
    }

    fun extractApiSpec(openAPI: OpenAPI): ApiSpec {
        return ApiSpec(
            servers = openAPI.servers.map { toApiServer(it) },
            name = openAPI.info.title,
            description = openAPI.info.description,
            security = openAPI.security ?: emptyList(),
            components = openAPI.components?.schemas ?: emptyMap(),
            paths = openAPI.paths.map { (path, pathItem) ->
                ApiPath(
                    fullPath = path,
                    path = splitPath(path, pathItem), // /user/{id}
                    operations = pathItem.readOperationsMap().map { (method, operation) ->
                        PathOperation(
                            method = toHttpMethod(method.name),
                            security = operation.security ?: emptyList(),
                            parameters = operation.parameters?.map { param ->
                                extractParameterInfo(param)
                            } ?: emptyList(),
                            requestBody = operation.requestBody?.let { reqBody ->
                                val mediaType = reqBody.content?.keys?.firstOrNull() ?: "unknown"
                                val schema = reqBody.content?.get(mediaType)?.schema
                                ApiRequestBody(
                                    contentType = mediaType,
                                    schemaType = extractType(schema),
                                    required = reqBody.required ?: false,
                                )
                            },
                            responses = operation.responses.map { (statusCode, response) ->
                                val contentType = response.content?.keys?.firstOrNull()
                                ApiResponse(
                                    statusCode = fromCode(statusCode) ?: StatusCode.UNKNOWN,
                                    contentType = contentType,
                                    schemaType = extractType(response.content?.get(contentType)?.schema)
                                )
                            },
                            servers = operation.servers?.map { it.url } ?: emptyList()
                        )
                    }
                )
            }
        )
    }

    fun splitPath(path: String, pathItem: PathItem): List<PathParts> {
        return path.split("/").filter { it.isNotBlank() }.map { part ->
            if (part.startsWith("{") && part.endsWith("}")) {
                val paramName = part.substring(1, part.length - 1)
                val param = pathItem.parameters?.find { it.name == paramName }
                val type = extractType(param?.schema)
                PathParts.Param(paramName, type)
            } else {
                PathParts.Static(part)
            }
        }
    }

    fun toApiServer(server: Server): ApiServer {
        return ApiServer(
            url = server.url,
            description = server.description,
            variables = server.variables?.map { (name, variable) ->
                ServerVariable(
                    name = name,
                    defaultValue = variable.default,
                    enum = variable.enum ?: emptyList()
                )
            } ?: emptyList()
        )
    }

    fun extractType(schema: Schema<*>?): Type {
        return when (schema) {
            is ObjectSchema -> ObjectType(
                fieldsTypes = schema.properties?.mapValues { (_, propertySchema) ->
                    extractType(propertySchema)
                } ?: emptyMap()
            )
            is ArraySchema -> ArrayType(
                elementsType = extractType(schema.items ?: Schema<Any>())
            )
            else -> when (schema?.type ?: "unknown") {
                "boolean" -> Type.BooleanType
                "number" -> Type.NumberType
                "string" -> Type.StringType
                "integer" -> Type.IntegerType
                else -> Type.UnknownType
            }
        }
    }

    fun extractParameterInfo(param: Parameter): ApiParameter {
        return ApiParameter(
            name = param.name ?: "unknown",
            type = extractType(param.schema),
            required = param.required ?: false,
            allowEmptyValue = param.allowEmptyValue ?: false,
            location = toParamLocation(param.`in`),
            style = toParamStyle(param.style.name),
            explode = param.explode ?: false,
            description = param.description
        )
    }

    private fun toParamLocation(location: String): Location {
        return when (location) {
            "query" -> Location.QUERY
            "header" -> Location.HEADER
            "path" -> Location.PATH
            "cookie" -> Location.COOKIE
            else -> Location.UNKNOWN
        }
    }

    private fun toParamStyle(style: String): ParameterStyle {
        return when (style) {
            "form" -> ParameterStyle.FORM
            "spaceDelimited" -> ParameterStyle.SPACEDELIMITED
            "pipeDelimited" -> ParameterStyle.PIPEDELIMITED
            "deepObject" -> ParameterStyle.DEEPOBJECT
            "simple" -> ParameterStyle.SIMPLE
            "matrix" -> ParameterStyle.MATRIX
            "label" -> ParameterStyle.LABEL
            else -> ParameterStyle.FORM
        }
    }

    private fun toHttpMethod(method: String): HttpMethod {
        return when (method.uppercase()) {
            "GET" -> HttpMethod.GET
            "POST" -> HttpMethod.POST
            "PUT" -> HttpMethod.PUT
            "DELETE" -> HttpMethod.DELETE
            "PATCH" -> HttpMethod.PATCH
            "OPTIONS" -> HttpMethod.OPTIONS
            "HEAD" -> HttpMethod.HEAD
            "TRACE" -> HttpMethod.TRACE
            else -> throw IllegalArgumentException("Unsupported method: $method")
        }
    }

}