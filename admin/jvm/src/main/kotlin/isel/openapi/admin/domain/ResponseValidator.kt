package isel.openapi.admin.domain

import isel.openapi.admin.http.model.ResponseConfig
import isel.openapi.admin.parsingServices.model.Response

class ResponseValidator(
    private val responses: List<Response>,
    private val adminDomain: AdminDomain,
) {

    fun validateResponse(responseConfig: ResponseConfig) {
        responses.any { response ->
            adminDomain.verifyResponse(response, responseConfig.statusCode, responseConfig.headers, responseConfig.body) !is VerifyResponseError
        }
    }
}
