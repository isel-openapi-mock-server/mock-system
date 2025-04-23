package isel.openapi.mock.repository

interface Transaction {

    val problemsRepository: ProblemsRepository

    val openAPIRepository: OpenAPIRepository

    fun rollback()
}