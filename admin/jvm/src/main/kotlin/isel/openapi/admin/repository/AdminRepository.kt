package isel.openapi.admin.repository

import isel.openapi.admin.domain.admin.SpecInfo

interface AdminRepository {

    fun addAPISpec(
        name: String,
        description: String?,
        transactionToken: String
    ): Int

    fun addPath(
        id: Int,
        path: String,
        operations: String,
    )

    fun getSpecId(
        host: String
    ): Int?

    fun updateAPISpec(
        id: Int,
        name: String,
        description: String?
    )

    fun getApiSpecByTransactionToken(
        transactionToken: String
    ): SpecInfo?

}