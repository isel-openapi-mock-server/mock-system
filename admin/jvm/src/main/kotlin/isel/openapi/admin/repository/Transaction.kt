package isel.openapi.admin.repository

interface Transaction {

    val adminRepository: AdminRepository

    val transactionsRepository: TransactionsRepository

    fun rollback()
}