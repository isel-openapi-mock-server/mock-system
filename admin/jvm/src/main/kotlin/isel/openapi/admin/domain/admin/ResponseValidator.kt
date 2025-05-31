package isel.openapi.admin.domain.admin

import isel.openapi.admin.http.model.ResponseConfig
import isel.openapi.admin.parsing.model.Response
import isel.openapi.admin.parsing.model.StatusCode

class ResponseValidator(
    private val responses: List<Response>,
    private val adminDomain: AdminDomain,
) {

    // TODO() retornar a lista de erros, mas cada resposta vai ter a sua, lista de listas?
    fun validateResponse(responseConfig: ResponseConfig): List<VerifyResponseError> {
        val failList = mutableListOf<VerifyResponseError>()
        val response = responses.firstOrNull { it.statusCode == StatusCode.fromCode(responseConfig.statusCode) }
        if(response == null) {
            failList.add(VerifyResponseError.WrongStatusCode)
            return failList
        }
        val errors = adminDomain.verifyResponse(
            response,
            StatusCode.fromCode(responseConfig.statusCode)!!,
            responseConfig.contentType,
            responseConfig.headers,
            responseConfig.body?.toByteArray()
        )

        errors.forEach { failList.add(it) }

        return failList
    }

}
