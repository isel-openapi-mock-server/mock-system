package isel.openapi.admin.parsing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.erosb.jsonsKema.JsonValue
import io.swagger.v3.oas.models.media.*
import isel.openapi.admin.parsing.model.*
import org.junit.jupiter.api.Assertions.assertEquals
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
        val op = parsing.extractApiSpec(definition).paths[1].operations[0]

        assertTrue { op.parameters.size == 1 }
        assertTrue { op.parameters[0].name == "id" }
        assertTrue { op.parameters[0].location == Location.PATH }
        assertTrue { op.parameters[0].required }
        assertTrue { op.parameters[0].type is ContentOrSchema.SchemaObject }

        assertTrue { op.headers[0].name == "num" }
        assertFalse { op.headers[0].required }
        assertTrue { op.headers[0].type is ContentOrSchema.SchemaObject }

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
        assertTrue { responses[0].schema == null }
        // assertTrue { responses[0].schema == Type.UnknownType }
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
        assertTrue { operations[0].security }
    }

    @Test
    fun refTest() {
        val isValid = parsing.validateOpenApi(openAPIDefinition3)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition3) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val operations = parsing.extractApiSpec(definition).paths[0].operations

        assertEquals(1, operations.size)
        assertEquals(HttpMethod.POST, operations[0].method)

        val objectMapper = ObjectMapper()

        val schema = JsonValue.parse(
            """
            {
              "required" : [ "email", "name", "password", "username" ],
              "type" : "object",
              "properties" : {
                "name" : {
                  "type" : "string",
                  "exampleSetFlag" : false,
                  "types" : [ "string" ]
                },
                "email" : {
                  "type" : "string",
                  "exampleSetFlag" : false,
                  "types" : [ "string" ]
                },
                "username" : {
                  "type" : "string",
                  "exampleSetFlag" : false,
                  "types" : [ "string" ]
                },
                "password" : {
                  "type" : "string",
                  "exampleSetFlag" : false,
                  "types" : [ "string" ]
                }
              },
              "exampleSetFlag" : false,
              "types" : [ "object" ]
            }
            """.trimIndent()
        )

        val expected = objectMapper.readTree(schema.toString())
        val actual = objectMapper.readTree(operations[0].requestBody?.content?.content?.get("application/json")?.schema?.toString() ?: "")

        assertEquals(expected, actual)
    }

    @Test
    fun extractResponse(){
        val isValid = parsing.validateOpenApi(openAPIDefinition1)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition1) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val responses = parsing.extractApiSpec(definition).paths[0].operations[0].responses

        assertEquals(2, responses.size)

        val response = responses[0]

        assertEquals(StatusCode.OK, response.statusCode)

        val responseHeaders = response.headers
        assertEquals(1, responseHeaders.size)

        assertEquals("A", responseHeaders[0].name)
        assertEquals(false, responseHeaders[0].required)
    }

    @Test
    fun extractBody() {

        val isValid = parsing.validateOpenApi(openAPIDefinition3)

        assertTrue { isValid }

        val definition = parsing.parseOpenApi(openAPIDefinition3) ?: throw IllegalStateException("Invalid OpenAPI definition")
        val operations = parsing.extractApiSpec(definition).paths[0].operations
        assertEquals(1, operations.size)
        val requestBody = operations[0].requestBody
        assertTrue { requestBody != null }
        assertTrue { requestBody?.content?.content?.containsKey("application/json") == true }
        val schema = requestBody?.content?.content?.get("application/json")?.schema

        val objectMapper = ObjectMapper()
        val i = objectMapper.readValue(schema, Schema::class.java)

        assertTrue { i is Schema }
        i as Schema
        assertTrue { i.type == "object" }
        assertTrue { i.properties != null }
        assertTrue { i.properties?.containsKey("name") == true }
        assertTrue { i.properties?.containsKey("email") == true }
        assertTrue { i.properties?.containsKey("username") == true }
        assertTrue { i.properties?.containsKey("password") == true }
        assertTrue { i.required?.contains("name") == true }
        assertTrue { i.required?.contains("email") == true }
        assertTrue { i.required?.contains("username") == true }
        assertTrue { i.required?.contains("password") == true }
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
                  headers:
                    A:
                      description: bom dia
                      schema:
                        type: string
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