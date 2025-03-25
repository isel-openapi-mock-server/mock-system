package isel.openapi.mock.repository

import isel.openapi.mock.utils.ApiParameter
import isel.openapi.mock.utils.extractApiSpec
import isel.openapi.mock.utils.parseOpenApi
import kotlin.test.Test
import kotlin.test.assertTrue

class DynamicRoutesMemTests {

    val dynamicRoutesMem = DynamicRoutesMem()

    @Test
    fun testAddDynamicRoute() {
        val definition = parseOpenApi(openAPIDefinition) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val info = extractApiSpec(definition)

        dynamicRoutesMem.addDynamicRoute(info.paths[0].path, info.paths[0].methods[0])

        val params = dynamicRoutesMem.getParams(info.paths[0].path, info.paths[0].methods[0])
        val body = dynamicRoutesMem.getBody(info.paths[0].path, info.paths[0].methods[0])

        assertTrue { params == emptyList<ApiParameter>() }
        assertTrue { body!!.required }
        assertTrue { body!!.parameters.keys == setOf("inviteCode", "username", "password") }

    }

    val openAPIDefinition = """
        openapi: 3.0.1
        info:
          title: ChIMP API
          version: 1.0.0
          description: API for Instant messaging application
        servers:
          - description: Localhost server for testing API
            url: http://localhost:8080/api
        paths:
          /users:
            post:
              tags:
                - Users
              summary: Create a new user
              requestBody:
                required: true
                content:
                  application/json:
                    schema:
                      type: object
                      properties:
                        inviteCode:
                          type: string
                          example: jFeqtSelG7Nj
                        username:
                          type: string
                          example: bob123
                        password:
                          type: string
                          example: password123
              responses:
                '201':
                  description: User created successfully
                '400':
                  description: Bad Request
                  content:
                    text/plain:
                      schema:
                        type: string
                        enum:
                          - User already exists
                          - Password is insecure
                          - Invalid username
                          - Invalid register code
                        example: User already exists
                '500':
                  description: Internal server error
    """.trimIndent()

}