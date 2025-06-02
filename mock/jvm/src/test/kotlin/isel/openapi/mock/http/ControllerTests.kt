package isel.openapi.mock.http

import isel.openapi.mock.domain.dynamic.DynamicDomain
import isel.openapi.mock.domain.problems.ProblemsDomain
import isel.openapi.mock.repository.DynamicRoutesRepository
import isel.openapi.mock.repository.JdbiProblemsRepositoryTests
import isel.openapi.mock.repository.TransactionManager
import isel.openapi.mock.repository.jdbi.JdbiTransactionManager
import isel.openapi.mock.repository.jdbi.configureWithAppRequirements
import isel.openapi.mock.services.DynamicHandlerServices
import isel.openapi.mock.services.Router
import jakarta.annotation.PostConstruct
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerTests {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun `can make a request for dynamic route`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.post().uri("/dynamic-routes/update").exchange()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("users/search")
                .queryParam("username", "diogo")
                .queryParam("limit", 2)
                .build()
        }
            .header("Host", "host1")
            .header("Scenario-name", "test1")
            .header("Authorization", "Bearer Token123456789012345678901234567890")
            .header("A", "bom dia")

            .exchange()
            .expectStatus().isOk
            .expectBody().json(
                """
            [
              {"id":5,"username":"diogo"},
              {"id":40,"username":"martim"}
            ]
            """.trimIndent()
            )
    }

    companion object {

        private val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL("jdbc:postgresql://localhost:5435/mock?user=mock&password=mock")
            }
        ).configureWithAppRequirements()

        val services = DynamicHandlerServices(
            router = Router(DynamicRoutesRepository(), DynamicDomain()),
            problemsDomain = ProblemsDomain(),
            transactionManager = JdbiTransactionManager(
                jdbi = jdbi
            )
        )

        fun notifyDB() {
            val handle = jdbi.open()
            handle.createUpdate("NOTIFY update_spec").execute()
            handle.close()
        }

    }

}