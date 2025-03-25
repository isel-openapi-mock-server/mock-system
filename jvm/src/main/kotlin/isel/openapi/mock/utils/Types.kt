package isel.openapi.mock.utils

import io.swagger.v3.oas.models.security.SecurityRequirement

data class ApiSpec(
    val paths: List<ApiPath>
)

data class ApiPath(
    val path: String,
    val methods: List<ApiMethod>
)

data class ApiMethod(
    val method: String,
    val security: List<SecurityRequirement>,
    val parameters: List<ApiParameter>,
    val requestBody: ApiRequestBody?,
    val responses: List<ApiResponse>
)

data class ApiParameter(
    val name: String,
    val type: String?,
    val required: Boolean,
    val allowEmptyValue: Boolean,// usado no parametro na query, "?param="
    val location: String, // "query", "header", "path", "cookie"
    val style: String, //
    val explode: Boolean,   // Se true: ?ids=1&ids=2&ids=3 é valido, se false: ?ids=1,2,3 é valido.
)

data class ApiRequestBody(
    val contentType: String,
    val schemaType: String?,
    val required: Boolean,
    val parameters: Map<String, Any>
)

data class ApiResponse(
    val statusCode: String,
    val contentType: String?
)