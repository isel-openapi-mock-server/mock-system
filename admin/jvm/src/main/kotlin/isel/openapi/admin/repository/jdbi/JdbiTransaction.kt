package isel.openapi.admin.repository.jdbi

import isel.openapi.admin.repository.AdminRepository
import org.jdbi.v3.core.Handle
import isel.openapi.admin.repository.Transaction

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {

    override val adminRepository: AdminRepository = JdbiAdminRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}