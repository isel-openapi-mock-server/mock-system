package isel.openapi.admin.repository

import isel.openapi.admin.domain.HeadersInfo
import isel.openapi.admin.domain.ProblemInfo
import isel.openapi.admin.domain.RequestInfo

interface AdminRepository {

    fun getRequestInfoExternalKey(
        externalKey: String
    ): List<RequestInfo>

    fun getRequestInfoUUID(
        uuid: String
    ) : RequestInfo?

    fun getRequestProblems(
        requestUUID: String
    ): List<ProblemInfo>

    fun getRequestBody(
        requestUUID: String
    ) : ByteArray?

    fun getRequestHeaders(
        requestUUID: String
    ) : List<HeadersInfo>

}