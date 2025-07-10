package isel.openapi.admin.repository

import isel.openapi.admin.domain.admin.AdminDomain
import isel.openapi.admin.repository.jdbi.JdbiTransactionsRepository
import isel.openapi.admin.repository.jdbi.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.io.encoding.Base64.Default.decode
import kotlin.test.*

class JdbiTransactionsRepositoryTests {

    @Test
    fun `transaction should be active`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val newTransaction = adminDomain.generateTokenValue()
            repo.addNewTransaction(newTransaction, 1, null, Clock.System.now().epochSeconds)

            val res = repo.isTransactionActive(newTransaction)

            assertTrue(res)
        }

    @Test
    fun `transaction should not be active`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val transactionId = "transaction1"

            val res = repo.isTransactionActive(transactionId)

            assertFalse(res)
        }

    @Test
    fun `transaction should commit transaction`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val newTransaction = adminDomain.generateTokenValue()
            repo.addNewTransaction(newTransaction, 1, null, Clock.System.now().epochSeconds)

            val res = repo.isTransactionActive(newTransaction)

            assertTrue(res)

            repo.commitTransaction(newTransaction, "host2")

            val res2 = repo.isTransactionActive(newTransaction)

            assertFalse(res2)
        }

    @Test
    fun `host should exist`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val host = "host1"

            val res = repo.isHostExists(host)

            assertTrue(res)
        }

    @Test
    fun `host should not exist`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val host = "banana"

            val res = repo.isHostExists(host)

            assertFalse(res)
        }

    @Test
    fun `transaction for host should exist`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val host = "host1"

            val res = repo.getTransactionByHost(host)

            assertEquals("transaction1", res)
        }


    @Test
    fun `transaction for host should not exist`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val host = "banana"

            val res = repo.getTransactionByHost(host)

            assertNull(res)
        }

    @Test
    fun `add new transaction`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)
            val now = Clock.System.now()

            val transactionToken = adminDomain.generateTokenValue()

            val host = "host5"

            repo.addNewTransaction(transactionToken, 1, host, now.epochSeconds)

            val res = repo.isTransactionActive(transactionToken)

            assertTrue(res)

        }

    @Test
    fun `copy spec to transaction`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)
            val now = Clock.System.now()

            val transactionId = adminDomain.generateTokenValue()

            val res = repo.copySpecToTransaction(transactionId, 1)

            repo.addNewTransaction(transactionId, res, "host1", now.epochSeconds)

            val id = repo.getSpecIdByTransaction(transactionId)

            assertEquals(res, id)

        }

    @Test
    fun `should get scenario name for transaction`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val transactionId = "transaction2"

            val res = repo.getScenarioNameByTransaction(transactionId)

            assertEquals("test2", res)

        }

    @Test
    fun `should not get scenario name for transaction`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val transactionId = "transaction1"

            val res = repo.getScenarioNameByTransaction(transactionId)

            assertNull(res)

        }

    @Test
    fun `should delete scenario`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val newTransaction = adminDomain.generateTokenValue()
            repo.addNewTransaction(newTransaction, 1, null, Clock.System.now().epochSeconds)
            repo.addScenario(newTransaction, "banana", "GET", "/users/search", 1, Clock.System.now().epochSeconds)

            val res = repo.deleteScenario(newTransaction, "banana")

            assertTrue(res)

        }

    @Test
    fun `should not delete scenario`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val transactionId = "transaction1"

            val scenarioName = "banana"

            val res = repo.deleteScenario(transactionId, scenarioName)

            assertFalse(res)

        }

    @Test
    fun `should add scenario`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)
            val now = Clock.System.now()

            val transactionId = "transaction1"

            val scenarioName = "scenario123"

            repo.addScenario(transactionId, scenarioName, "GET", "/users/search", 1, now.epochSeconds)

            val res = repo.deleteScenario(transactionId, scenarioName)

            assertTrue(res)

        }

    @Test
    fun `should add scenario response`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val scenarioName = adminDomain.generateTokenValue()

            val newTransaction = adminDomain.generateTokenValue()
            repo.addNewTransaction(newTransaction, 2, null, Clock.System.now().epochSeconds)
            repo.addScenario(newTransaction, scenarioName, "GET", "/users/search", 2, Clock.System.now().epochSeconds)

            val res = repo.addScenarioResponse(
                transactionToken = newTransaction,
                scenarioName = scenarioName,
                index = 0,
                statusCode = "200",
                body = null,
                headers = null,
                contentType = null,
                specId = 2
            )

            assertTrue(res)

        }

    @Test
    fun `should get specId from transaction token`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val transactionId = "transaction1"

            val res = repo.getSpecIdByTransaction(transactionId)

            assertEquals(1, res)

        }

    @Test
    fun `should not get specId from transaction token`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val transactionId = "banana"

            val res = repo.getSpecIdByTransaction(transactionId)

            assertNull(res)
        }

    @Test
    fun `should get host from transaction token`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val transactionId = "transaction1"

            val res = repo.getHostByTransactionToken(transactionId)

            assertEquals("host1", res)

        }

    @Test
    fun `should not get host from transaction token`() =
        runWithHandle { handle ->
            val repo = JdbiTransactionsRepository(handle)

            val transactionId = "banana"

            val res = repo.getHostByTransactionToken(transactionId)

            assertNull(res)

        }

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val adminDomain = AdminDomain()

        private val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL("jdbc:postgresql://localhost:5434/admin?user=mock&password=mock")
                },
            ).configureWithAppRequirements()

        val clock = Clock
    }
}