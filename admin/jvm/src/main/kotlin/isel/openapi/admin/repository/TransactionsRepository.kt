package isel.openapi.admin.repository

interface TransactionsRepository {

    fun isTransactionActive(uuid: String): Boolean

    fun commitTransaction(uuid: String, host: String)

    fun isHostExists(host: String): Boolean
}