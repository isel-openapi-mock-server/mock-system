package isel.openapi.admin.repository

interface Transaction {

    val adminRepository: AdminRepository

    fun rollback()
}