package isel.openapi.admin.repository

interface Transaction {

    val adminRepository: AdminRepository

    val requestsRepository: RequestsRepository

    val transactionsRepository: TransactionsRepository

    fun rollback()
}