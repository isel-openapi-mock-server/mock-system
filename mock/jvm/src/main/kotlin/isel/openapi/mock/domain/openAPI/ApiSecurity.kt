package isel.openapi.mock.domain.openAPI

interface ApiSecurity {
}

class ApiKeySecurity(
    val name: String,
    val inLocation: Location,
) : ApiSecurity

class OAuth2Security(
    val flows: Map<String, OAuth2Flow>
) : ApiSecurity

class HttpSecurity(
    val scheme: String,
) : ApiSecurity

class OpenIdConnectSecurity(
    val openIdUrl: String
) : ApiSecurity

class OAuth2Flow(
    val authorizationUrl: String,
    val tokenUrl: String,
    val scopes: Map<String, String>
)

/**
 *
 */

/**
 * Expected response //host, path, method, statusCode = null, responseBody = null, responseHeaders = null
 */

/**
 * response(host: String, path: String, method: HttpMethod, statusCode: StatusCode)
 * .responseBody(body: String)
 * .responseHeaders(headers: Map<String, String>)
 *
 * response.save()
 */

/**
 * PUT
 * Expected response
 */