package isel.openapi.mock.http

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

    @Test
    fun `host does not exists`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.post().uri("/dynamic-routes/update").exchange()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("users/search")
                .queryParam("username", "diogo")
                .queryParam("limit", 2)
                .build()
        }
            .header("Host", "host123")
            .header("Authorization", "Bearer Token123456789012345678901234567890")
            .header("A", "bom dia")

            .exchange()
            .expectStatus().isBadRequest
            .expectBody().json(
                """
              {"type":"https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/host-does-not-exist"}
            """.trimIndent()
            )
    }

}