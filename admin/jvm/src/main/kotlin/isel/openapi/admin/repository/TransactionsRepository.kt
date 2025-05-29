package isel.openapi.admin.repository

interface TransactionsRepository {

    fun isTransactionActive(uuid: String): Boolean

    fun commitTransaction(uuid: String, host: String)

    fun isHostExists(host: String): Boolean

    fun getTransactionByHost(uuid: String): String?

    fun addNewTransaction(uuid: String, host: String)

    fun copySpecToTransaction(
        transactionToken: String,
        specId: Int
    ): Int
    
    fun getScenarioNameByTransaction(
        transactionToken: String
    ): String?

    fun deleteScenario(
        transactionToken: String,
        scenarioName: String
    ): Boolean

    fun addScenario(
        transactionToken: String,
        scenarioName: String
    ): Boolean

    fun addScenarioResponse(
        transactionToken: String,
        scenarioName: String,
        index: Int,
        statusCode: Int,
        body: ByteArray?,
        headers: String?,
    ): Boolean

}