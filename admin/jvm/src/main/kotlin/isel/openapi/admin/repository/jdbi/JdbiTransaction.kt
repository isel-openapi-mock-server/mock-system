package isel.openapi.admin.repository.jdbi

import isel.openapi.admin.repository.AdminRepository
import isel.openapi.admin.repository.RequestsRepository
import org.jdbi.v3.core.Handle
import isel.openapi.admin.repository.Transaction
import isel.openapi.admin.repository.TransactionsRepository

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {

    override val adminRepository: AdminRepository = JdbiAdminRepository(handle)

    override val requestsRepository: RequestsRepository = JdbiRequestsRepository(handle)

    override val transactionsRepository: TransactionsRepository = JdbiTransactionsRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}