package isel.openapi.mock.parsingServices

import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.StringSchema
import isel.openapi.mock.parsingServices.model.HttpMethod
import isel.openapi.mock.parsingServices.model.Location
import isel.openapi.mock.parsingServices.model.Type
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParsingTests {

    val parsing = Parsing()

    @Test
    fun infoParsingTest() {

        val isValid = parsing.validateOpenApi(openAPIDefinition)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val info = parsing.extractApiSpec(definition)

        assertTrue { info.name == "API" }
        assertTrue { info.description == null }
        assertTrue { info.servers.size == 1 }
        assertTrue { info.servers[0].url == "https://api.exemplo.com/" }
    }

    @Test
    fun operationsParsingTest() {

        val isValid = parsing.validateOpenApi(openAPIDefinition)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val operations = parsing.extractApiSpec(definition).paths[1].operations

        assertTrue { operations.size == 3 }
        assertTrue { operations[0].method == HttpMethod.GET }
        assertTrue { operations[1].method == HttpMethod.PUT }
        assertTrue { operations[2].method == HttpMethod.DELETE }
    }

    @Test
    fun parametersParsingTest() {

        val isValid = parsing.validateOpenApi(openAPIDefinition)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val parameters = parsing.extractApiSpec(definition).paths[1].operations[0].parameters

        assertTrue { parameters.size == 2 }
        assertTrue { parameters[0].name == "id" }
        assertTrue { parameters[0].location == Location.PATH }
        assertTrue { parameters[0].required }
        assertTrue { parameters[0].type == Type.StringType }

        assertTrue { parameters[1].name == "num" }
        assertTrue { parameters[1].location == Location.HEADER }
        assertFalse { parameters[1].required }
        assertTrue { parameters[1].type == Type.ArrayType(Type.IntegerType) }

    }

    @Test
    fun responsesParsingTest() {

        val isValid = parsing.validateOpenApi(openAPIDefinition)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val responses = parsing.extractApiSpec(definition).paths[1].operations[0].responses

        assertTrue { responses.size == 1 }
        assertTrue { responses[0].statusCode.code == 200 }
        assertTrue { responses[0].statusCode.name == "OK" }
        assertTrue { responses[0].contentType == null }
        assertTrue { responses[0].schemaType == Type.UnknownType }
    }

    @Test
    fun invalidOpenApiDefinitionTest() {

        val isValid = parsing.validateOpenApi("openapi: 3.0.4")

        assertFalse { isValid }
    }

    @Test
    fun extractObjectTypeTest() {
        val userSchema = ObjectSchema().apply {
            properties = mapOf(
                "id" to IntegerSchema(),
                "name" to StringSchema(),
                "email" to StringSchema()
            )
        }
        val extractedType = parsing.extractType(userSchema)

        assertTrue { extractedType == Type.ObjectType(
            mapOf(
                "id" to Type.IntegerType,
                "name" to Type.StringType,
                "email" to Type.StringType
            )
        ) }

    }

    @Test
    fun extractArrayTypeTest() {
        val arraySchema = ArraySchema().apply {
            items = IntegerSchema()
        }
        val extractedType = parsing.extractType(arraySchema)

        assertTrue { extractedType == Type.ArrayType(Type.IntegerType) }
    }

    @Test
    fun securityParsingTest() {
        val isValid = parsing.validateOpenApi(openAPIDefinition1)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition1) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val operations = parsing.extractApiSpec(definition).paths[0].operations

        assertTrue { operations.size == 1 }
        assertTrue { operations[0].method == HttpMethod.GET }
        assertTrue { operations[0].security.size == 1 }
        assertTrue { operations[0].security[0].containsKey("BearerAuth") }
    }

    @Test
    fun refTest() {
        val isValid = parsing.validateOpenApi(openAPIDefinition3)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition3) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val operations = parsing.extractApiSpec(definition).paths[0].operations

        assertTrue { operations.size == 1 }
        assertTrue { operations[0].method == HttpMethod.POST }
        assertTrue { operations[0].requestBody?.schemaType == Type.ObjectType(
            mapOf(
                "name" to Type.StringType,
                "email" to Type.StringType,
                "username" to Type.StringType,
                "password" to Type.StringType
            )
        ) }
        assertTrue {
            operations[0].responses[0].schemaType == Type.ObjectType(
                mapOf(
                    "token" to Type.StringType,
                    "username" to Type.StringType,
                    "id" to Type.IntegerType
                )
            )
        }
    }

    val openAPIDefinition = """
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

    val openAPIDefinition1 = """
        openapi: 3.0.1
        info:
          title: ChIMP API
          version: 1.0.0
          description: API for Instant messaging application
        servers:
          - description: Localhost server for testing API
            url: http://localhost:8080/api
        paths:
          /users/search:
            get:
              tags:
                - Users
              summary: Search for a user by username
              security:
                - BearerAuth: []
              parameters:
                - name: username
                  in: query
                  required: true
                  schema:
                    type: string
                - name: limit
                  in: query
                  required: false
                  schema:
                    type: integer
                - name: skip
                  in: query
                  required: false
                  schema:
                    type: integer
        
              responses:
                '200':
                  description: User retrieved successfully
                  content:
                    application/json:
                      schema:
                        type: array
                        items:
                          type: object
                          properties:
                            id:
                              type: integer
                              example: 1
                            username:
                              type: string
                              example: bob123
                '500':
                  description: Internal server error
        components:
          securitySchemes:
            BearerAuth:
              type: http
              scheme: bearer
              description: |
                Bearer token for authentication. Format: "Bearer {token}"
              bearerFormat: JWT
    """.trimIndent()

    val openAPIDefinition3 = """
    openapi: 3.0.1
    info:
      title: ProjLS API
      description: API for projLs
      version: 1.0.0
    servers:
      - description: Localhost server for testing API
        url: http://localhost:8080
    paths:
      /players:
        description: The resource to create a new player
        post:
          tags:
            - Players
          summary: Adds a player
          description: Adds a player to the system given a name and email
          operationId: postPlayer
          requestBody:
            description: Player to add
            content:
              application/json:
                schema:
                  __REF__: '#/components/schemas/PlayerInput'
            required: true
          responses:
            201:
              description: Player created
              content:
                application/json:
                  schema:
                    __REF__: '#/components/schemas/PlayerOutput'
    components:
      schemas:
        PlayerInput:
          type: object
          required:
            - name
            - email
            - username
            - password
          properties:
            name:
              type: string
            email:
              type: string
            username:
              type: string
            password:
              type: string
        PlayerOutput:
          type: object
          required:
            - token
            - username
            - id
          properties:
            token:
              type: string
              format: uuid
            username:
              type: string
            id:
              type: integer
""".trimIndent().replace("__REF__", "\$ref")


}