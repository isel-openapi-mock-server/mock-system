package isel.openapi.admin.http

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerTests {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun `addOpenApiSpec returns 400 for invalid OpenAPI spec`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.post().uri("/admin/openapi")
            .contentType(MediaType("application", "json"))
            .bodyValue(emptyOpenAPISpec)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .json(
                """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-open-api-spec.txt"}
            """.trimIndent()
            )
    }

    @Test
    fun `addOpenApiSpec returns 201 for valid OpenAPI spec`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.post().uri("/admin/openapi")
            .contentType(MediaType("application", "json"))
            .bodyValue(openAPIDefinition)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
    }

    @Test
    fun `addResponseConfig returns 400 for invalid transaction`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.put().uri { uriBuilder ->
            uriBuilder
                .path("/admin/response")
                .build()
        }
            .contentType(MediaType.APPLICATION_JSON)
            .header("Transaction-token", "invalid-token")
            .bodyValue("""
                {
                    "name": "test",
                    "path": "/api/test",
                    "method": "GET",
                    "responses": [
                        {
                            "statusCode": "200",
                            "contentType": null,
                            "headers": null,
                            "body": null
                        }
                    ]
                }
            """.trimIndent())
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .json(
                """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-transaction.txt"}
            """.trimIndent()
            )
    }

    @Test
    fun `addResponseConfig returns 400 for host does not exist`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.put().uri { uriBuilder ->
            uriBuilder
                .path("/admin/response")
                .queryParam("host", "mirtilo")
                .build()
        }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "name": "test",
                    "path": "/api/test",
                    "method": "GET",
                    "responses": [
                        {
                            "statusCode": "200",
                            "contentType": null,
                            "headers": null,
                            "body": null
                        }
                    ]
                }               
            """.trimIndent())
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
    fun `addResponseConfig returns 400 for no transaction token and host`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.put().uri { uriBuilder ->
            uriBuilder
                .path("/admin/response")
                .build()
        }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "name": "test",
                    "path": "/api/test",
                    "method": "GET",
                    "responses": [
                        {
                            "statusCode": "200",
                            "contentType": null,
                            "headers": null,
                            "body": null
                        }
                    ]
                }
            """.trimIndent())
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .json(
                """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/transaction-or-host-not-provided.txt"}
            """.trimIndent()
            )
    }

    @Test
    fun `addResponseConfig returns 404 for path operation doesn't exist`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.put().uri { uriBuilder ->
            uriBuilder
                .path("/admin/response")
                .queryParam("host", "host1")
                .build()
        }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "name": "nonexistent",
                    "path": "/api/nonexistent",
                    "method": "GET",
                    "responses": [
                        {
                            "statusCode": "200",
                            "contentType": null,
                            "headers": null,
                            "body": null
                        }
                    ]
                }
            """.trimIndent())
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .json(
                """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/path-operation-does-not-exist.txt"}
            """.trimIndent()
            )
    }

    @Test
    fun `addResponseConfig returns 400 for invalid scenario`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.put().uri { uriBuilder ->
            uriBuilder
                .path("/admin/response")
                .queryParam("host", "host1")
                .build()
        }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
            {
                "name": "test-scenario",
                "path": "/users/search",
                "method": "GET",
                "responses": [
                    {
                        "statusCode": 400",
                    }
                ]
            }
            """.trimIndent()
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            /*.json(
                """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-response-content.txt"}
            """.trimIndent()
            )*/
    }

    @Test
    fun `addResponseConfig returns 201 for valid scenario`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.put().uri { uriBuilder ->
            uriBuilder
                .path("/admin/response")
                .queryParam("host", "host1")
                .build()
        }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
            {
                "name": "test-scenario",
                "path": "/users/search",
                "method": "GET",
                "responses": [
                    {
                        "statusCode": "200",
                        "contentType": "application/json",
                        "headers": null,
                        "body": "[{\"id\":1,\"username\":\"bob123\"}]"
                    }
                ]
            }
            """.trimIndent()
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
    }


    @Test
    fun `commitChanges returns 400 for host does not exist`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.post().uri { uriBuilder ->
            uriBuilder
                .path("/admin/commit")
                .queryParam("host", "mirtilo")
                .build()
        }
            .header("Transaction-token", "transaction2")
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
    fun `commitChanges returns 400 for invalid transaction`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.post().uri { uriBuilder ->
            uriBuilder
                .path("/admin/commit")
                .build()
        }
            .header("Transaction-token", "invalid-token")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .json(
            """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-transaction.txt"}
            """.trimIndent()
            )
    }

    @Test
    fun `commitChanges returns 200`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.post().uri { uriBuilder ->
            uriBuilder
                .path("/admin/commit")
                .build()
        }
            .header("Transaction-token", "test2")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
        .json(
            """
              {"type":"https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-transaction.txt"}
            """.trimIndent()
            )
    }

    @Test
    fun `delete transaction 2 open transactions`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        client.delete().uri { uriBuilder ->
            uriBuilder
                .path("/admin/transactions")
                .build()
        }
            .exchange()
            .expectStatus().isOk
    }

    private val emptyOpenAPISpec =
        """
        {
            "spec": ""
        }
        """

    private val openAPIDefinition =
        """
        {
            "spec": "openapi: 3.0.1\ninfo:\n  title: ChIMP API\n  version: 1.0.0\n  description: API for Instant messaging application\nservers:\n  - description: Localhost server for testing API\n    url: http://localhost:8080/api\npaths:\n  /users:\n    post:\n      tags:\n        - Users\n      summary: Create a new user\n      requestBody:\n        required: true\n        content:\n          application/json:\n            schema:\n              type: object\n              properties:\n                inviteCode:\n                  type: string\n                  example: jFeqtSelG7Nj\n                username:\n                  type: string\n                  example: bob123\n                password:\n                  type: string\n                  example: password123\n      responses:\n        '201':\n          description: User created successfully\n        '400':\n          description: Bad Request\n          content:\n            text/plain:\n              schema:\n                type: string\n                enum:\n                  - User already exists\n                  - Password is insecure\n                  - Invalid username\n                  - Invalid register code\n                example: User already exists\n        '500':\n          description: Internal server error\n\n  /users/token:\n    post:\n      tags:\n        - Users\n      summary: Authenticate user and get token\n      requestBody:\n        required: true\n        content:\n          application/json:\n            schema:\n              type: object\n              properties:\n                username:\n                  type: string\n                  example: bob123\n                password:\n                  type: string\n                  example: password123\n      responses:\n        '200':\n          description: Token generated successfully\n          content:\n            application/json:\n              schema:\n                type: object\n                properties:\n                  token:\n                    type: string\n                    example: Xa3KdPtDjDfI7v8mYQ2zWnJ5m7iHrPOf2D3_YcPtE8I\n        '400':\n          description: User or Password Invalid\n          content:\n            text/plain:\n              schema:\n                type: string\n                example: User or password are invalid\n        '500':\n          description: Internal server error\n  /users/search:\n    get:\n      tags:\n        - Users\n      summary: Search for a user by username\n      security:\n        - BearerAuth: []\n      parameters:\n        - name: username\n          in: query\n          required: true\n          schema:\n            type: string\n        - name: limit\n          in: query\n          required: false\n          schema:\n            type: integer\n        - name: skip\n          in: query\n          required: false\n          schema:\n            type: integer\n\n      responses:\n        '200':\n          description: User retrieved successfully\n          content:\n            application/json:\n              schema:\n                type: array\n                items:\n                  type: object\n                  properties:\n                    id:\n                      type: integer\n                      example: 1\n                    username:\n                      type: string\n                      example: bob123\n        '500':\n          description: Internal server error\n  /users/{id}:\n    get:\n      tags:\n        - Users\n      summary: Gets user by id\n      security:\n        - BearerAuth: []\n      parameters:\n        - name: id\n          in: path\n          required: true\n          schema:\n            type: integer\n      responses:\n        '200':\n          description: User retrieved successfully\n          content:\n            application/json:\n              schema:\n                type: object\n                properties:\n                  id:\n                    type: integer\n                    example: 1\n                  username:\n                    type: string\n                    example: bob123\n        '400':\n          description: Invalid request\n          content:\n            text/plain:\n              schema:\n                type: string\n                example: User doesn't exist\n        '500':\n          description: Internal server error\n  /logout:\n    post:\n      tags:\n        - Users\n      summary: Terminates user's session\n      security:\n        - BearerAuth: []\n      responses:\n        '200':\n          description: User's session terminated successfully\n        '500':\n          description: Internal server error\n\n  /me/channels:\n    get:\n      tags:\n        - Users\n      summary: Gets user channels\n      security:\n        - BearerAuth: []\n      responses:\n        '200':\n          description: Retrieved user's channels successfully\n          content:\n            application/json:\n              schema:\n                type: object\n                properties:\n                  channels:\n                    type: array\n                    items:\n                      type: object\n                      properties:\n                        id:\n                          type: integer\n                          example: 1\n                        name:\n                          type: string\n                          example: Channel A\n                        description:\n                          type: string\n                          example: chat\n                        lastMessageDate:\n                          type: integer\n                          example: 17993482349\n                        noNewMessages:\n                          type: integer\n                          example: 2\ncomponents:\n  securitySchemes:\n    BearerAuth:\n      type: http\n      scheme: bearer\n      description: |\n        Bearer token for authentication. Format: \"Bearer {token}\""
        }
        """
}