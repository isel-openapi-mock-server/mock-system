package isel.openapi.admin.repository.jdbi

import isel.openapi.admin.domain.requests.ProblemInfo
import isel.openapi.admin.domain.requests.RequestDetails
import isel.openapi.admin.domain.requests.RequestInfo
import isel.openapi.admin.domain.requests.RequestResponse
import isel.openapi.admin.domain.requests.ResponseBody
import isel.openapi.admin.domain.requests.ResponseInfo
import isel.openapi.admin.repository.RequestsRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiRequestsRepository(
    private val handle: Handle,
) : RequestsRepository {

    override fun getRequestInfoExchangeKey(
        exchangeKey: String
    ): RequestInfo? {
        val temp = handle.createQuery(
            """
            SELECT external_key, path_template, method, host, uuid FROM requests WHERE uuid = :uuid
            """
        )
            .bind("uuid", exchangeKey)
            .mapTo<RequestDetails>()
            .firstOrNull()

        if(temp == null) return null

        val problems = getRequestProblems(exchangeKey)

        val body = getRequestBody(exchangeKey)

        val response = if(problems.isEmpty()) {
            getResponse(exchangeKey)
        } else {
            null
        }

        return RequestInfo(
            exchangeKey = exchangeKey,
            externalKey = temp.externalKey,
            method = temp.method,
            path = temp.pathTemplate,
            host = temp.host,
            body = body,
            problems = problems,
            response = response
        )
    }

    override fun getRequestInfoExternalKey(
        externalKey: String
    ): List<RequestInfo> {

        val toReturn = mutableListOf<RequestInfo>()

        val requestTemp = handle.createQuery(
            """
            SELECT external_key, path_template, method, host, uuid FROM requests WHERE external_key = :externalKey
            """
        )
            .bind("externalKey", externalKey)
            .mapTo<RequestDetails>()
            .list()

        requestTemp.forEach {
            val problems = getRequestProblems(it.uuid)

            val body = getRequestBody(it.uuid)

            val response = if(problems.isEmpty()) {
                getResponse(it.uuid)
            } else {
                null
            }

            toReturn.add(
                RequestInfo(
                    exchangeKey = it.uuid,
                    externalKey = externalKey,
                    method = it.method,
                    path = it.pathTemplate,
                    host = it.host,
                    body = body,
                    problems = problems
                )
            )
        }

        return toReturn

    }

    override fun getRequestProblems(
        requestUUID: String,
    ): List<ProblemInfo> {

        return handle.createQuery(
            """
            SELECT description, type FROM problems WHERE uuid = :uuid
            """
        )
            .bind("uuid", requestUUID)
            .mapTo<ProblemInfo>()
            .list()

    }

    override fun getRequestBody(
        requestUUID: String,
    ): ByteArray? {

        return handle.createQuery(
            """
            SELECT content FROM request_body WHERE uuid = :uuid
            """
        )
            .bind("uuid", requestUUID)
            .mapTo<ByteArray>() //Base64
            .firstOrNull()
    }

    override fun searchRequests(
        host: String,
        method: String?,
        path: String?,
        startDate: Long?,
        endDate: Long?
    ): List<RequestInfo> {

        val query = StringBuilder(
            """
            SELECT r.uuid, r.external_key, r.path_template, r.method, r.host FROM requests AS r
            WHERE r.host = :host
            """
        )

        if (method != null) {
            query.append(" AND r.method = :method")
        }

        if (path != null) {
            query.append(" AND r.resolved_path LIKE :path")
        }

        if (startDate != null && endDate != null && startDate > endDate) {
            query.append(" AND r.date >= :startDate AND r.date <= :endDate")
        }

        val requests = handle.createQuery(
            query.toString()
        )
            .bind("host", host)
            .bind("method", method)
            .bind("path", path?.let { "%$it%" })
            .bind("startDate", startDate)
            .bind("endDate", endDate)
            .mapTo<RequestDetails>()
            .list()

        val toReturn = mutableListOf<RequestInfo>()

        requests.forEach { request ->
            val problems = getRequestProblems(request.uuid)

            val body = getRequestBody(request.uuid)

            toReturn.add(
                RequestInfo(
                    exchangeKey = request.uuid,
                    externalKey = request.externalKey,
                    method = request.method,
                    path = request.pathTemplate,
                    host = request.host,
                    body = body,
                    problems = problems
                )
            )
        }
        return toReturn
    }

    override fun getResponse(requestUUID: String): ResponseInfo {
        val response = handle.createQuery(
            """
            SELECT id, status_code FROM responses WHERE uuid = :uuid
            """
        )
            .bind("uuid", requestUUID)
            .mapTo<RequestResponse>()
            .first()

        val responseBody = handle.createQuery(
            """
            SELECT content, content_type FROM response_body WHERE response_id = :id
            """
        )
            .bind("id", response.id)
            .mapTo<ResponseBody>()
            .firstOrNull()

        return ResponseInfo(
            body = responseBody?.content,
            contentType = responseBody?.contentType,
            statusCode = response.statusCode
        )

    }

}