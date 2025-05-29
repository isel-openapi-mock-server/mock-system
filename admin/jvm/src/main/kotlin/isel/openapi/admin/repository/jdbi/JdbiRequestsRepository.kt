package isel.openapi.admin.repository.jdbi

import isel.openapi.admin.domain.requests.HeadersInfo
import isel.openapi.admin.domain.requests.ProblemInfo
import isel.openapi.admin.domain.requests.RequestDetails
import isel.openapi.admin.domain.requests.RequestInfo
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
            SELECT external_key, url, method, host, uuid FROM requests WHERE uuid = :uuid
            """
        )
            .bind("uuid", exchangeKey)
            .mapTo<RequestDetails>()
            .firstOrNull()

        if(temp == null) return null

        val problems = getRequestProblems(exchangeKey)

        val headers = getRequestHeaders(exchangeKey)

        val body = getRequestBody(exchangeKey)

        return RequestInfo(
            exchangeKey = exchangeKey,
            externalKey = temp.externalKey,
            method = temp.method,
            path = temp.url,
            host = temp.host,
            body = body,
            headers = headers,
            problems = problems
        )
    }

    override fun getRequestInfoExternalKey(
        externalKey: String
    ): List<RequestInfo> {

        val toReturn = mutableListOf<RequestInfo>()

        val requestTemp = handle.createQuery(
            """
            SELECT external_key, url, method, host, uuid FROM requests WHERE external_key = :externalKey
            """
        )
            .bind("externalKey", externalKey)
            .mapTo<RequestDetails>()
            .list()

        requestTemp.forEach {
            val problems = getRequestProblems(it.uuid)

            val headers = getRequestHeaders(it.uuid)

            val body = getRequestBody(it.uuid)

            toReturn.add(
                RequestInfo(
                    exchangeKey = it.uuid,
                    externalKey = externalKey,
                    method = it.method,
                    path = it.url,
                    host = it.host,
                    body = body,
                    headers = headers,
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

    override fun getRequestHeaders(
        requestUUID: String,
    ): List<HeadersInfo> {

        return handle.createQuery(
            """
            SELECT name, content FROM request_headers WHERE uuid = :uuid
            """
        )
            .bind("uuid", requestUUID)
            .mapTo<HeadersInfo>()
            .toList()

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
            SELECT r.uuid, r.external_key, r.url, r.method, r.host, FROM requests AS r
            WHERE r.host = :host
            """
        )

        if (method != null) {
            query.append(" AND r.method = :method")
        }

        if (path != null) {
            query.append(" AND r.path LIKE :path")
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

            val headers = getRequestHeaders(request.uuid)

            val body = getRequestBody(request.uuid)

            toReturn.add(
                RequestInfo(
                    exchangeKey = request.uuid,
                    externalKey = request.externalKey,
                    method = request.method,
                    path = request.url,
                    host = request.host,
                    body = body,
                    headers = headers,
                    problems = problems
                )
            )
        }
        return toReturn
    }
}