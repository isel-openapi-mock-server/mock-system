package isel.openapi.mock.repository.jdbi

import isel.openapi.mock.repository.ProblemsRepository
import org.jdbi.v3.core.Handle
import isel.openapi.mock.repository.Transaction

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {

    override val problemsRepository: ProblemsRepository = JdbiProblemsRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}