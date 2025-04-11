package isel.openapi.mock.repository

import isel.openapi.mock.http.VerificationError
import isel.openapi.mock.parsingServices.model.ApiParameter

interface ProblemsRepository {

    fun addRequest(uuid: String, url: String, method: String, path: String, externalKey: String)

    fun addRequestParams(uuid: String, params: List<ApiParameter>)

    fun addRequestBody(uuid: String, body: ByteArray, contentType: String)

    fun addProblems(uuid: String, problems: List<VerificationError>)

    fun addResponse(uuid: String, statusCode: Int, body: ByteArray, contentType: String)

    fun addResponseHeaders(uuid: String, headers: Map<String, String>)

}