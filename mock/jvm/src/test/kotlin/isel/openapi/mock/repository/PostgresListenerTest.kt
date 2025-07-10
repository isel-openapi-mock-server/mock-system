package isel.openapi.mock.repository

import isel.openapi.mock.repository.jdbi.configureWithAppRequirements
import org.postgresql.ds.PGSimpleDataSource
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi

class PostgresListenerTest {

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) =
            jdbi.useTransaction<Exception>(block)

        private val dataSource = PGSimpleDataSource().apply {
            setURL("jdbc:postgresql://localhost:5435/mock?user=mock&password=mock")
        }

        private val jdbi = Jdbi.create(
            dataSource
        ).configureWithAppRequirements()
    }


}