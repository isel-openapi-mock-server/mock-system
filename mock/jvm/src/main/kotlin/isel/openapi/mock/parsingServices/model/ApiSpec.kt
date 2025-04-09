package isel.openapi.mock.parsingServices.model

import io.swagger.v3.oas.models.security.SecurityRequirement

data class ApiSpec(
    val name: String,
    val description: String?,
    val servers: List<ApiServer>,
    val paths: List<ApiPath>,
    val security: List<SecurityRequirement>,
    val components : Map<String, Any>,
)