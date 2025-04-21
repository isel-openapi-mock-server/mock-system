package isel.openapi.admin.repository.jdbi

import isel.openapi.admin.domain.HeadersInfo
import isel.openapi.admin.domain.ProblemInfo
import isel.openapi.admin.domain.RequestDetails
import isel.openapi.admin.domain.RequestInfo
import isel.openapi.admin.repository.AdminRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiAdminRepository(
    private val handle: Handle,
) : AdminRepository {

    override fun getRequestInfoUUID(
        uuid: String
    ): RequestInfo? {
        val temp = handle.createQuery(
            """
            SELECT external_key, url, method, host, uuid FROM request_info WHERE uuid = :uuid
            """
        )
            .bind("uuid", uuid)
            .mapTo<RequestDetails>()
            .firstOrNull()

        if(temp == null) return null

        val problems = getRequestProblems(uuid)

        val headers = getRequestHeaders(uuid)

        val body = getRequestBody(uuid)

        return RequestInfo(
            uuid = uuid,
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
            SELECT external_key, url, method, host, uuid FROM request_info WHERE external_key = :externalKey
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
                    uuid = it.uuid,
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
            .mapTo<ByteArray>()
            .firstOrNull()

    }
}