package isel.openapi.admin.repository

import isel.openapi.admin.repository.jdbi.JdbiRequestsRepository
import isel.openapi.admin.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertTrue
import org.postgresql.ds.PGSimpleDataSource
import kotlin.test.Test
import kotlin.test.assertEquals

class JdbiRequestRepositoryTests {

    @Test
    fun `get request info with exchange key`() =
        runWithHandle { handle ->
            val repo = JdbiRequestsRepository(handle)

            val exchangeKey = "request1"

            val reqInfo = repo.getRequestInfoExchangeKey(exchangeKey)

            assertTrue(reqInfo != null)

            assertEquals("type2", reqInfo!!.externalKey)

        }

    @Test
    fun `get request info with external key`() =
        runWithHandle { handle ->
            val repo = JdbiRequestsRepository(handle)

            val externalKey = "type2"

            val reqInfo = repo.getRequestInfoExternalKey(externalKey)

            assertEquals(2, reqInfo.size)

        }


    @Test
    fun `get requests problems`() =
        runWithHandle { handle ->
            val repo = JdbiRequestsRepository(handle)

            val requestId = "request2"

            val probInfo = repo.getRequestProblems(requestId)

            assertEquals(1, probInfo.size)
        }

/*
    @Test
    fun `get request body`() =
        runWithHandle { handle ->
            val repo = JdbiRequestsRepository(handle)

            val requestId = "request2"

            val reqBody = repo.getRequestBody(requestId)

        }
*/

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL("jdbc:postgresql://localhost:5434/admin?user=mock&password=mock")
                },
            ).configureWithAppRequirements()
    }
}