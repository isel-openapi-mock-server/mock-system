package isel.openapi.mock.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class ParsingTests {

    @Test
    fun generalParsingTest() {

        val isValid = validateOpenApi(openAPIDefinition)

        assertTrue { isValid }

        val definition = parseOpenApi(openAPIDefinition) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val info = extractApiSpec(definition)

        assertTrue { info.paths.size == 2 }
        assertTrue { info.paths[0].path == "/api" }
        assertTrue { info.paths[1].path == "/api/{id}" }

        assertTrue { info.paths[0].methods.size == 2 }

        val methods = info.paths[0].methods
        assertTrue { methods[0].method == "GET" }

    }

    @Test
    fun invalidParsingTest() {
        val isValid = validateOpenApi("openapi: 3.0.0")
        assertTrue { !isValid }
    }

    @Test
    fun validateParameters() {
        val definition = parseOpenApi(openAPIDefinition2) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val info = extractApiSpec(definition)

        val parameter = info.paths[0].methods[0].parameters[0]
        assertTrue { parameter.name == "id" }
        assertTrue { parameter.location == "path" }
        assertTrue { parameter.required }
        assertTrue { parameter.type == "string" }
    }

    @Test
    fun validateResponses() {
        val definition = parseOpenApi(openAPIDefinition2) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val info = extractApiSpec(definition)

        val responses = info.paths[0].methods[0].responses
        assertTrue { responses.size == 1 }
        assertTrue { responses[0].statusCode == "200" }
        assertTrue { responses[0].contentType == null }
    }

    @Test
    fun validateRequestBody() {
        val definition = parseOpenApi(openAPIDefinition3) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val info = extractApiSpec(definition)

        val requestBody = info.paths[0].methods[0].requestBody
        assertTrue { requestBody != null }
        assertTrue { requestBody!!.contentType == "application/json" }
        assertTrue { requestBody!!.schemaType == "object" }
        assertTrue { requestBody!!.required }
        assertTrue { requestBody!!.parameters.size == 3 }
        assertTrue { requestBody!!.parameters["inviteCode"] == "string" }
    }

    val openAPIDefinition = """
        openapi: 3.0.0
        info:
          title: "API"
          version: "1.0"
        servers:
          - url: "https://api.exemplo.com"
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

    val openAPIDefinition2 = """
        openapi: 3.0.0
        info:
          title: "API"
          version: "1.0"
        paths:
          /api/{id}:
            get:
              parameters:
                - name: id
                  in: path
                  required: true
                  schema:
                    type: string
              responses:
                '200':
                  description: "OK"
            put:
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

    val openAPIDefinition3 = """
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