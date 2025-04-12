package isel.openapi.mock.repository

interface Transaction {

    val problemsRepository: ProblemsRepository

    fun rollback()
}