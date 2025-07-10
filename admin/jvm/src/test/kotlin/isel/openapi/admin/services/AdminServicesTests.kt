package isel.openapi.admin.services

import isel.openapi.admin.domain.admin.AdminDomain
import isel.openapi.admin.http.model.ResponseConfig
import isel.openapi.admin.http.model.Scenario
import isel.openapi.admin.parsing.Parsing
import isel.openapi.admin.repository.ResolverRepository
import isel.openapi.admin.repository.jdbi.JdbiTransactionManager
import isel.openapi.admin.repository.jdbi.configureWithAppRequirements
import isel.openapi.admin.utils.Failure
import isel.openapi.admin.utils.Success
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminServicesTests {

    @Test
    fun `saveNewSpec successful result`() {
        val adminServices = createAdminServices()

        val result = adminServices.saveNewSpec(openAPIDefinition)

        assertTrue(result is Success)
    }

    @Test
    fun `saveNewSpec unsuccessful result`() {
        val adminServices = createAdminServices()

        val result = adminServices.saveNewSpec("invalid spec")

        assertTrue(result is Failure)
        assertEquals(CreateSpecError.InvalidOpenApiSpec, result.value)
    }

    @Test
    fun `saveResponseConfig should detect missing transaction token and host`() {
        val adminServices = createAdminServices()

        val res = adminServices.saveResponseConfig(
            null,
            Scenario("", "", "", emptyList()),
            null
        )

        assertTrue(res is Failure)
        assertEquals(SaveScenarioError.TransactionOrHostNotProvided, res.value)
    }

    @Test
    fun `saveResponseConfig should detect already committed transaction`() {
        val adminServices = createAdminServices()

        val res = adminServices.saveResponseConfig(
            "host1",
            makeScenario("scenario1"),
            "transaction1"
        )

        assertTrue(res is Failure)
        assertEquals(SaveScenarioError.InvalidTransaction, res.value)
    }

    @Test
    fun `saveResponseConfig should detect non existent transaction`() {
        val adminServices = createAdminServices()

        val res = adminServices.saveResponseConfig(
            "host1",
            makeScenario("scenario1"),
            "banana"
        )

        assertTrue(res is Failure)
        assertEquals(SaveScenarioError.InvalidTransaction, res.value)
    }

    @Test
    fun `saveResponseConfig should detect non existent host`() {
        val adminServices = createAdminServices()

        val res = adminServices.saveResponseConfig(
            "banana",
            makeScenario("scenario1"),
            null
        )

        assertTrue(res is Failure)
        assertEquals(SaveScenarioError.HostDoesNotExist, res.value)
    }

    @Test
    fun `saveResponseConfig should detect non existent path operation`() {
        val adminServices = createAdminServices()

        val res = adminServices.saveResponseConfig(
            "host1",
            Scenario(
                name = "scenario1",
                path = "banana",
                method = "GET",
                responses = listOf(
                    ResponseConfig(
                        statusCode = "200",
                        contentType = null,
                        headers = null,
                        body = null,
                    )
                )
            ),
            null
        )

        assertTrue(res is Failure)
        assertEquals(SaveScenarioError.PathOperationDoesNotExist, res.value)
    }

    @Test
    fun `saveResponseConfig should detect invalid response content in headers`() {
        val adminServices = createAdminServices()

        val res = adminServices.saveResponseConfig(
            "host1",
            Scenario(
                name = "scenario1",
                path = "/users/search",
                method = "GET",
                responses = listOf(
                    ResponseConfig(
                        statusCode = "OK",
                        contentType = null,
                        headers = mapOf("invalid-header" to "asd"),
                        body = null,
                    )
                )
            ),
            null
        )

        assertTrue(res is Failure)
        //assertEquals(SaveScenarioError.InvalidResponseContent, res.value)
        assertTrue(res.value is SaveScenarioError.InvalidResponseContent)
    }


    @Test
    fun `saveResponseConfig should detect invalid response content in body`() {
        val adminServices = createAdminServices()

        val res = adminServices.saveResponseConfig(
            "host1",
            Scenario(
                name = "scenario1",
                path = "/users/search",
                method = "GET",
                responses = listOf(
                    ResponseConfig(
                        statusCode = "OK",
                        contentType = "application/json",
                        headers = null,
                        body = "{ invalid: [1, 2, }"
                    )
                )
            ),
            null
        )

        assertTrue(res is Failure)
        //assertEquals(SaveScenarioError.InvalidResponseContent, res.value)
        assertTrue(res.value is SaveScenarioError.InvalidResponseContent)
    }

    @Test
    fun `saveResponseConfig should succeed`() {
        val adminServices = createAdminServices()

        val res = adminServices.saveResponseConfig(
            "host1",
            Scenario(
                name = "scenario1",
                path = "/users/search",
                method = "GET",
                responses = listOf(
                    ResponseConfig(
                        statusCode = "200",
                        contentType = "application/json",
                        headers = mapOf("A" to "bom dia"),
                        body = """
                            [
                                {
                                    "id": 1,
                                    "username": "bob123"
                                }
                            ]
                        """.trimIndent()
                    )
                )
            ),
            null
        )

        assertTrue(res is Success)
    }


    companion object {
        private val adminDomain = AdminDomain()

        private fun makeScenario(name: String) = Scenario(
            name = name,
            path = "/users/search",
            method = "GET",
            responses = listOf(
                ResponseConfig(
                    statusCode = "200",
                    contentType = null,
                    headers = null,
                    body = null,
                )
            )
        )

        private val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL("jdbc:postgresql://localhost:5434/admin?user=mock&password=mock")
                },
            ).configureWithAppRequirements()

        private fun createAdminServices() = AdminServices(
            parsing = Parsing(),
            transactionManager = JdbiTransactionManager(jdbi),
            adminDomain = adminDomain,
            router = RouteValidatorResolver(
                repository = ResolverRepository(),
                dynamicDomain = adminDomain
            ),
            clock = kotlinx.datetime.Clock.System
        )
    }

    private val openAPIDefinition = """
        openapi: 3.0.4
        info:
          title: "API"
          version: "1.0"
        servers:
          - url: "https://api.exemplo.com/"
        paths:
          /api:
            get:
              summary: "Obtém informações gerais"
              responses:
                '200':
                  description: "OK"
            post:
              summary: "Cria um novo recurso"
              responses:
                '201':
                  description: "Created"
          /api/{id}:
            get:
              summary: "Obtém um recurso específico"
              parameters:
                - name: id
                  in: path
                  required: true
                  schema:
                    type: string
                - name: num
                  in: header
                  schema:
                    type: array
                    items:
                      type: integer
              responses:
                '200':
                  description: "OK"
            put:
              summary: "Atualiza um recurso específico"
              parameters:
                - name: id
                  in: path
                  required: true
                  schema:
                    type: string
              responses:
                '200':
                  description: "OK"
            delete:
              summary: "Remove um recurso específico"
              parameters:
                - name: id
                  in: path
                  required: true
                  schema:
                    type: string
              responses:
                '204':
                  description: "No Content"
        """.trimIndent()

}
