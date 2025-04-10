package isel.openapi.mock.repository.jdbi

import org.jdbi.v3.core.Handle
import isel.openapi.mock.repository.Transaction

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {

    override fun rollback() {
        handle.rollback()
    }
}