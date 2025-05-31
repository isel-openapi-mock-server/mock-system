package isel.openapi.mock.repository

import isel.openapi.mock.domain.dynamic.DynamicDomain
import isel.openapi.mock.domain.problems.ProblemsDomain
import isel.openapi.mock.repository.jdbi.JdbiTransactionManager
import isel.openapi.mock.repository.jdbi.configureWithAppRequirements
import isel.openapi.mock.services.DynamicHandlerServices
import isel.openapi.mock.services.Router
import isel.openapi.mock.services.Synchronizer
import org.postgresql.ds.PGSimpleDataSource
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import kotlin.test.Test

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