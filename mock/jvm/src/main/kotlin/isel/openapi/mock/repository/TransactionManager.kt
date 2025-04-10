package isel.openapi.mock.repository

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}