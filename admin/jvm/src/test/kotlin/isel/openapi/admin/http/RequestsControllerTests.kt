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
            .expectBody()
            .json(
                """
      [{
        "exchangeKey": "request1",
        "externalKey": "type2",
        "method": "GET",
        "path": "/users/search",
        "host": "host1",
        "body": null,
        "problems": [],
        "response": {
          "body": "W3siaWQiOjUsInVzZXJuYW1lIjoiZGlvZ28ifSx7ImlkIjo0MCwidXNlcm5hbWUiOiJtYXJ0aW0ifV0=",
          "statusCode": 200,
          "contentType": "application/json"
        }
      }]
    """.trimIndent()
            )

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

    @Test
    fun `getRequestInfo returns 404 when request not found`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("admin/requests")
                .queryParam("exchangeKey", "nonexistent")
                .build()
        }
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `getRequestInfo returns 400 when credentials are required`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("admin/requests")
                .build()
        }
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `searchRequests returns 400 when host doesn't exist`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("admin/requests/search")
                .queryParam("host", "nonexistent-host")
                .queryParam("method", "GET")
                .build()
        }
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .json(
                """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/host-does-not-exist.txt"}
            """.trimIndent()
            )
    }

    @Test
    fun `searchRequests returns 400 when invalid date range`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("admin/requests/search")
                .queryParam("host", "host1")
                .queryParam("method", "GET")
                .queryParam("startDate", "123")
                .queryParam("endDate", "12")
                .build()
        }
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .json(
                """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-date-range.txt"}
            """.trimIndent()
            )
    }

    @Test
    fun `searchRequests returns 400 when invalid method`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("admin/requests/search")
                .queryParam("host", "host1")
                .queryParam("method", "invalid-method")
                .build()
        }
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .json(
                """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-method.txt"}
            """.trimIndent()
            )
    }

    @Test
    fun `searchRequests returns 200`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.get().uri { uriBuilder ->
            uriBuilder
                .path("admin/requests/search")
                .queryParam("host", "host1")
                .queryParam("method", "GET")
                .build()
        }
            .exchange()
            .expectStatus().isOk
    }
}
