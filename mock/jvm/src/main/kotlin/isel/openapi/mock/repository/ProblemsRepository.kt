package isel.openapi.mock.repository

import isel.openapi.mock.domain.problems.ParameterInfo
import isel.openapi.mock.http.VerificationError

interface ProblemsRepository {

    fun addRequest(uuid: String, pathTemplate: String, method: String, resolvedPath: String, externalKey: String?, host: String, headers: String?, date: Long)

    fun addRequestParams(uuid: String, params: List<ParameterInfo>)

    fun addRequestBody(uuid: String, body: ByteArray, contentType: String)

    fun addProblems(uuid: String, problems: List<VerificationError>)

    fun addResponse(uuid: String, statusCode: Int, headers: String?): Int

    fun addResponseBody(id: Int, body: ByteArray, contentType: String)

}