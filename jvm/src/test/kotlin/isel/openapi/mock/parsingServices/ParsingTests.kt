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


}