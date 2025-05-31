package isel.openapi.mock.repository

import isel.openapi.mock.domain.openAPI.ContentOrSchema
import isel.openapi.mock.domain.openAPI.Location
import isel.openapi.mock.domain.problems.ParameterInfo
import isel.openapi.mock.http.VerifyParamsError
import isel.openapi.mock.repository.jdbi.JdbiProblemsRepository
import isel.openapi.mock.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import kotlin.test.assertEquals

class JdbiProblemsRepositoryTests {

    @Test
    fun `can add request and info`() {
        runWithHandle { handle ->
            val repo = JdbiProblemsRepository(handle)
            val testRepo = RequestRepository(handle)

            repo.addRequest(
                uuid = "request1",
                url = "/users/search",
                method = "GET",
                path = "/users/search",
                externalKey = "external1",
                host = "host1",
                headers = "{\"A\": \"bom dia\"}"
            )

            repo.addRequestParams(
                uuid = "request1",
                params = listOf(
                    ParameterInfo(
                        name = "username",
                        content = "bob123",
                        location = Location.QUERY,
                        type = ContentOrSchema.SchemaObject(
                            schema = "{ \"type\": \"string\", \"exampleSetFlag\": false, \"types\": [\"string\"] }"
                        )
                    ),
                    ParameterInfo(
                        name = "limit",
                        content = "a",
                        location = Location.QUERY,
                        type = ContentOrSchema.SchemaObject(
                            schema = "{ \"type\": \"integer\", \"exampleSetFlag\": false, \"types\": [\"integer\"] }"
                        ),
                    )
                )
            )

            repo.addProblems(
                uuid = "request1",
                problems = listOf(
                    VerifyParamsError.InvalidParam(
                        location = Location.QUERY,
                        paramName = "limit"
                    ),
                )
            )

            val result = testRepo.getRequestInfoExchangeKey("request1")

            assert(result != null)
            assertEquals(expected.externalKey, result!!.externalKey)
            assertEquals(expected.method, result.method)
            assertEquals(expected.path, result.path)
            assertEquals(expected.host, result.host)
            assertEquals(expected.exchangeKey, result.exchangeKey)
            assertEquals(expected.problems.size, result.problems.size)
            assertEquals(expected.problems[0].description, result.problems[0].description)
            assertEquals(expected.problems[0].type, result.problems[0].type)
            assertEquals(expected.body, result.body)

        }

    }

    val expected = RequestInfo(
        exchangeKey = "request1",
        externalKey = "external1",
        method = "GET",
        path = "/users/search",
        host = "host1",
        body = null,
        problems = listOf(
            ProblemInfo(
                description = "Invalid parameter 'limit' in query",
                type = "isel.openapi.mock.http.VerifyParamsError.InvalidParam"
            )
        )
    )

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) =
            jdbi.useTransaction<Exception>(block)

        private val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL("jdbc:postgresql://localhost:5435/mock?user=mock&password=mock")
            }
        ).configureWithAppRequirements()

        data class RequestInfo(
            val exchangeKey: String,
            val externalKey: String,
            val method: String,
            val path: String,
            val host: String,
            val body: ByteArray?,
            val problems: List<ProblemInfo>,
        )

        data class ProblemInfo(
            val description: String,
            val type: String,
        )

        data class RequestDetails(
            val externalKey: String,
            val url: String,
            val method: String,
            val host: String,
            val uuid: String
        )

        interface RequestsRepository {
            fun getRequestInfoExchangeKey(externalKey: String): RequestInfo?
            fun getRequestProblems(requestUUID: String): List<ProblemInfo>
        }

        class RequestRepository(
            private val handle: Handle
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
                    .mapTo(RequestDetails::class.java)
                    .firstOrNull()

                if(temp == null) return null

                val problems = getRequestProblems(exchangeKey)

                return RequestInfo(
                    exchangeKey = exchangeKey,
                    externalKey = temp.externalKey,
                    method = temp.method,
                    path = temp.url,
                    host = temp.host,
                    body = null,
                    problems = problems
                )
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
                    .mapTo(ProblemInfo::class.java)
                    .list()

            }

        }

    }
}