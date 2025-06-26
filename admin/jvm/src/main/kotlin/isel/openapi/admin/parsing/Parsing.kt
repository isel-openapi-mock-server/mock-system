package isel.openapi.admin.parsing

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.parser.OpenAPIV3Parser
import isel.openapi.admin.parsing.model.*
import isel.openapi.admin.parsing.model.StatusCode.Companion.fromCode
import isel.openapi.admin.parsing.model.Type.ArrayType
import isel.openapi.admin.parsing.model.Type.ObjectType
import org.springframework.stereotype.Component
import kotlin.collections.component1
import kotlin.collections.component2

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

        //map<String, Schema<*>>
        // string -> SchemaName
        // #/components/schemas/SchemaName
        val allSchemas = openAPI.components?.schemas ?: emptyMap()
        val allResponse = openAPI.components?.responses ?: emptyMap()
        val allParameters = openAPI.components?.parameters ?: emptyMap()
        val allHeaders = openAPI.components?.headers ?: emptyMap()
        val security = openAPI.security

        return ApiSpec(
            name = openAPI.info.title,
            description = openAPI.info.description,
            paths = openAPI.paths.map { (path, pathItem) ->
                ApiPath(
                    fullPath = path,
                    path = splitPath(path), // /user/{id}
                    operations = pathItem.readOperationsMap().map { (method, operation) ->
                        PathOperation(
                            method = toHttpMethod(method.name),
                            security = !(operation.security == null && security == null),
                            parameters = extractParameters(
                                operation?.parameters?.filter { it.`in` != "header" } ?: emptyList(),
                                allParameters,
                            ),
                            requestBody = extractRequestBody(
                                operation?.requestBody ?: RequestBody(),
                                allSchemas
                            ),
                            responses = extractResponses(
                                operation.responses,
                                allResponse
                            ),
                            servers = operation.servers?.map { it.url } ?: emptyList(),
                            headers = extractHeaders(
                                operation?.parameters?.filter { it.`in` == "header" } ?: emptyList(),
                                allHeaders
                            ),
                        )
                    }
                )
            }
        )
    }

    fun extractHeaders(
        headers: List<Parameter>,
        allHeaders: Map<String?, Header?>,
    ): List<ApiHeader> {

        if(headers.isEmpty()) return emptyList()
        return headers.map { param ->

            val content = param.content

            if(content != null) {
                val map = mutableMapOf<String, ContentOrSchema.SchemaObject>()
                content.forEach { (key, value) ->
                    map[key] = ContentOrSchema.SchemaObject(schemaToJson(value.schema))
                }
                val p = extractParameterInfo(param, ContentOrSchema.ContentField(map)) //TODO
                return@map ApiHeader(
                    name = p.name,
                    description = p.description,
                    type = p.type,
                    required = p.required,
                    style = p.style,
                    explode = p.explode
                )
            } else {
                val schema = param.schema
                if (schema?.`$ref` != null) {
                    val ref = schema.`$ref`
                    val headerName = ref.substringAfterLast("/")
                    val refHeader = allHeaders[ref.substringAfterLast("/")]
                    return@map extractHeaderInfo(refHeader, headerName, ContentOrSchema.SchemaObject(schemaToJson(refHeader?.schema!!)))
                }
                val p = extractParameterInfo(param, ContentOrSchema.SchemaObject(schemaToJson(schema))) //TODO
                return@map ApiHeader(
                    name = p.name,
                    description = p.description,
                    type = p.type,
                    required = p.required,
                    style = p.style,
                    explode = p.explode
                )
            }
        }
    }

    fun extractHeaderInfo(
        header: Header?,
        headerName: String,
        type: ContentOrSchema
    ): ApiHeader {
        return ApiHeader(
            name = headerName,
            description = header?.description,
            type = type,
            required = header?.required == true,
            style = toParamStyle(header?.style?.name ?: ""),
            explode = header?.explode == true
        )
    }

    fun extractParameters(
        parameters: List<Parameter>,
        allParameter: Map<String?, Parameter?>,
    ): List<ApiParameter> {

        if(parameters.isEmpty()) return emptyList()

        return parameters.map { param ->
            val content = param.content
            if(content != null) {
                val map = mutableMapOf<String, ContentOrSchema.SchemaObject>()
                content.forEach { (key, value) ->
                    map[key] = ContentOrSchema.SchemaObject(schemaToJson(value.schema))
                }
                /*return@map*/ extractParameterInfo(param, ContentOrSchema.ContentField(map))
            }
            else {
                val schema = param.schema
                if (schema?.`$ref` != null) {
                    val ref = schema.`$ref`
                    val refParameter = allParameter[ref.substringAfterLast("/")]
                    return@map extractParameterInfo(refParameter!!, ContentOrSchema.SchemaObject(schemaToJson(refParameter.schema)))
                }
                extractParameterInfo(param, ContentOrSchema.SchemaObject(schemaToJson(schema)))
            }
        }
    }

    fun extractRequestBody(
        requestBody: RequestBody,
        allSchemas: Map<String?, Schema<*>>
    ): ApiRequestBody {

        val map = mutableMapOf<String, ContentOrSchema.SchemaObject>()

        requestBody.content?.forEach { (key, value) ->
            val schema = value.schema
            if (schema?.`$ref` != null) {
                val ref = schema.`$ref`
                val refSchema = allSchemas[ref.substringAfterLast("/")]
                map[key] = ContentOrSchema.SchemaObject(schemaToJson(refSchema!!))
            }
            else {
                map[key] = ContentOrSchema.SchemaObject(schemaToJson(schema ?: Schema<Any>()))
            }
        }

        return ApiRequestBody(
            content = ContentOrSchema.ContentField(map),
            required = requestBody.required ?: false,
        )
    }

    fun extractResponses(responses: ApiResponses?, allResponse: Map<String?, ApiResponse?>): List<Response> {

        if (responses == null) return emptyList()

        return responses.map { (statusCode, response) ->
            //TODO() faltam os headers, response.headers
            var responseHeaders = response?.headers ?: emptyMap()
            var contentTypes = response.content
            if(response.`$ref` != null) {
                val ref = response.`$ref`
                val refResponse = allResponse[ref.substringAfterLast("/")]
                contentTypes = refResponse?.content
                responseHeaders = refResponse?.headers ?: emptyMap()
            }
            val content = mutableMapOf<String, ContentOrSchema.SchemaObject>()
            contentTypes?.forEach { contType, mediaType ->
                content[contType] = ContentOrSchema.SchemaObject(schemaToJson(mediaType.schema))
            }
            val headers = mutableListOf<ApiHeader>()
            responseHeaders.forEach { (headerName, header) ->
                headers.add(extractHeaderInfo(header, headerName, ContentOrSchema.SchemaObject(schemaToJson(header.schema))))
            }
            Response(
                statusCode = fromCode(statusCode) ?: StatusCode.UNKNOWN,
                schema = if (content.isNotEmpty()) ContentOrSchema.ContentField(content = content) else null,
                headers = headers,
            )
        }
    }

    fun splitPath(path: String): List<PathParts> {
        return path.split("/").filter { it.isNotBlank() }.map { part ->
            if (part.startsWith("{") && part.endsWith("}")) {
                val paramName = part.substring(1, part.length - 1)
                PathParts(paramName, true)
            } else {
                PathParts(part, false)
            }
        }
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

    fun extractParameterInfo(param: Parameter, type: ContentOrSchema): ApiParameter {
        return ApiParameter(
            name = param.name ?: "unknown",
            type = type,
            required = param.required ?: false,
            allowEmptyValue = param.allowEmptyValue ?: false,
            location = toParamLocation(param.`in`),
            style = toParamStyle(param.style.name),
            explode = param.explode ?: false,
            description = param.description
        )
    }

    private fun schemaToJson(schema: Schema<*>): String {
        val objectMapper = ObjectMapper()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return objectMapper.writeValueAsString(schema)
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