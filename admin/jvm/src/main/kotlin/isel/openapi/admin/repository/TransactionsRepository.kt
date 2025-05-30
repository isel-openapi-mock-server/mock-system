package isel.openapi.admin.repository

import isel.openapi.admin.http.model.Scenario
import isel.openapi.admin.parsing.model.ApiPath

interface TransactionsRepository {

    fun isTransactionActive(uuid: String): Boolean

    fun commitTransaction(uuid: String, host: String)

    fun isHostExists(host: String): Boolean

    fun getTransactionByHost(uuid: String): String?

    fun addNewTransaction(
        uuid: String,
        specId: Int,
        host: String?
    )

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
        scenarioName: String,
        method: String,
        path: String,
    ): Boolean

    fun addScenarioResponse(
        transactionToken: String,
        scenarioName: String,
        index: Int,
        statusCode: String,
        body: ByteArray?,
        headers: String?,
        contentType: String? = null,
        specId: Int
    ): Boolean

    fun getSpecIdByTransaction(
        transactionToken: String
    ): Int?

}