package isel.openapi.mock.repository.jdbi

import isel.openapi.mock.repository.Transaction
import isel.openapi.mock.repository.TransactionManager
import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component

@Component
class JdbiTransactionManager(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}