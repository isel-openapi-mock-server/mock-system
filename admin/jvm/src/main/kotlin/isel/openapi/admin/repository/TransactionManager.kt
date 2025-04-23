package isel.openapi.admin.repository

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}