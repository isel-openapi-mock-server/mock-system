package isel.openapi.admin.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import isel.openapi.admin.domain.admin.AdminDomain
import isel.openapi.admin.parsing.model.PathOperation
import isel.openapi.admin.parsing.model.HttpMethod
import isel.openapi.admin.repository.jdbi.JdbiAdminRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.test.*
import isel.openapi.admin.repository.jdbi.configureWithAppRequirements

class JdbiAdminRepositoryTests {

    @Test
    fun `add API spec`() =
        runWithHandle { handle ->
            val repo = JdbiAdminRepository(handle)

            val transToken = adminDomain.generateTokenValue()

            val specName = "spec123"

            repo.addAPISpec(specName, null, transToken)

            val specInfo = repo.getApiSpecByTransactionToken(transToken)

            assertTrue(specInfo != null)

            assertEquals(specName, specInfo.name)
        }


    @Test
    fun `add path`() =
        runWithHandle { handle ->
            val repo = JdbiAdminRepository(handle)

            val transToken = adminDomain.generateTokenValue()

            val specId = repo.addAPISpec("spec", null, transToken)

            val path = "/a"

            val operations = listOf(
                PathOperation(
                    method = HttpMethod.GET,
                    security = false,
                    parameters = listOf(),
                    requestBody = null,
                    responses = listOf(),
                    servers = listOf(),
                    headers = listOf(),
                )
            )

            val operationsJson = mapper.writeValueAsString(operations)

            repo.addPath(specId, path, operationsJson)

            val specInfo = repo.getApiSpecByTransactionToken(transToken)

            assertTrue(specInfo != null)

            val pathOp = specInfo.paths.firstOrNull{it.path == path}

            assertTrue(pathOp != null)
        }

    @Test
    fun `can get spec id`() =
        runWithHandle { handle ->
            val repo = JdbiAdminRepository(handle)

            val specId = repo.getSpecId("host1") ?: fail("spec must exist")

            assertEquals(1, specId)
        }

    @Test
    fun `update API spec`() =
        runWithHandle { handle ->
            val repo = JdbiAdminRepository(handle)

            val transToken = adminDomain.generateTokenValue()

            val name = "specName"

            val specId = repo.addAPISpec(name, null, transToken)

            val specInfo = repo.getApiSpecByTransactionToken(transToken)

            assertTrue(specInfo != null)

            assertEquals(name, specInfo.name)

            val newName = "newSpecName"

            repo.updateAPISpec(specId, newName, null)

            val specInfo2 = repo.getApiSpecByTransactionToken(transToken)

            assertTrue(specInfo2 != null)

            assertEquals(newName, specInfo2.name)

        }

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val adminDomain = AdminDomain()

        private val mapper = jacksonObjectMapper()
            .registerKotlinModule()

        private val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL("jdbc:postgresql://localhost:5434/admin?user=mock&password=mock")
                },
            ).configureWithAppRequirements()
    }
}