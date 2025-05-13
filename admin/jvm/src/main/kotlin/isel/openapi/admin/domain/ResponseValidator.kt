package isel.openapi.admin.domain

import isel.openapi.admin.http.model.ResponseConfig
import isel.openapi.admin.parsingServices.model.Response
import isel.openapi.admin.parsingServices.model.StatusCode
import isel.openapi.admin.utils.Either

sealed interface VerifyResponseError {
    data object WrongStatusCode : VerifyResponseError
}

typealias VerifyResponseResult = Either<VerifyResponseError, Boolean>

class ResponseValidator(
    private val responses: List<Response>,
    private val adminDomain: AdminDomain,
) {

    fun validateResponse(responseConfig: ResponseConfig) {
        val response = responses.find { response ->
            response.statusCode == responseConfig.statusCode
        } ?: return

    }
}
