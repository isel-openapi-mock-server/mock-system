package isel.openapi.mock.repository

import isel.openapi.mock.domain.problems.ParameterInfo
import isel.openapi.mock.http.VerificationError

interface ProblemsRepository {

    fun addRequest(uuid: String, url: String, method: String, path: String, externalKey: String?, host: String, headers: String?)

    fun addRequestParams(uuid: String, params: List<ParameterInfo>)

    fun addRequestBody(uuid: String, body: ByteArray, contentType: String)

    fun addProblems(uuid: String, problems: List<VerificationError>)

    fun addResponse(uuid: String, statusCode: Int, headers: String?): Int

    fun addResponseBody(id: Int, body: ByteArray, contentType: String)

}