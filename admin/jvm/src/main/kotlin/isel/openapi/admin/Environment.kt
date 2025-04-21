package isel.openapi.admin

object Environment {
    fun getDbUrl() = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres"//System.getenv(KEY_DB_URL) ?: throw Exception("Missing env var $KEY_DB_URL")

    private const val KEY_DB_URL = "DB_URL"
}