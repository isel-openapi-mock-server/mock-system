package isel.openapi.admin.repository

import isel.openapi.admin.domain.requests.HeadersInfo
import isel.openapi.admin.domain.requests.ProblemInfo
import isel.openapi.admin.domain.requests.RequestInfo
import isel.openapi.admin.domain.requests.ResponseInfo

interface RequestsRepository {

    fun getRequestInfoExternalKey(
        externalKey: String
    ): List<RequestInfo>

    fun getRequestInfoExchangeKey(
        exchangeKey: String
    ) : RequestInfo?

    fun getRequestProblems(
        requestUUID: String
    ): List<ProblemInfo>

    fun getRequestBody(
        requestUUID: String
    ) : ByteArray?

    fun searchRequests(
        host: String,
        method: String?,
        path: String?,
        startDate: Long?,
        endDate: Long?
    ) : List<RequestInfo>

    fun getResponse(
        requestUUID: String
    ) : ResponseInfo

}