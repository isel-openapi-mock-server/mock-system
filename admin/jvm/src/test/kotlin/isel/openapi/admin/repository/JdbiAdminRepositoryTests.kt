package isel.openapi.admin.repository

import isel.openapi.admin.repository.jdbi.JdbiAdminRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.test.*
import isel.openapi.admin.repository.jdbi.configureWithAppRequirements

class JdbiAdminRepositoryTests {
    @Test
    fun `can get spec id`() =
        runWithHandle { handle ->
            val repo = JdbiAdminRepository(handle)

            val specId = repo.getSpecId("host1") ?: fail("spec must exist")

            assertEquals(1, specId)
        }

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