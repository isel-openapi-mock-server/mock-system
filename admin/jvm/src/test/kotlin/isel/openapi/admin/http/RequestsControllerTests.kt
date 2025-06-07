package isel.openapi.admin.http

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RequestsControllerTests {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun `test getRequestInfo with valid exchangeKey`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("admin/requests")
                .queryParam("exchangeKey", "request1")
                .build()
        }
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `test getRequestInfo with valid externalKey`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("admin/requests")
                .queryParam("externalKey", "type2")
                .build()
        }
            .exchange()
            .expectStatus().isOk
    }


    companion object {

    }
}