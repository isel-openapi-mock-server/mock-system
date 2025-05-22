package isel.openapi.admin.domain

import isel.openapi.admin.http.model.ResponseConfig
import isel.openapi.admin.parsingServices.model.Response

class ResponseValidator(
    private val responses: List<Response>,
    private val adminDomain: AdminDomain,
) {

    // TODO() retornar a lista de erros, mas cada resposta vai ter a sua, lista de listas?
    fun validateResponse(responseConfig: ResponseConfig): List<VerifyResponseError> {
        val failList = mutableListOf<VerifyResponseError>()
        responses.forEach { response ->
            failList.addAll(
                adminDomain.verifyResponse(
                    response,
                    responseConfig.statusCode,
                    responseConfig.contentType,
                    responseConfig.headers,
                    responseConfig.body
                )
            )
        }
        return failList
    }

}
