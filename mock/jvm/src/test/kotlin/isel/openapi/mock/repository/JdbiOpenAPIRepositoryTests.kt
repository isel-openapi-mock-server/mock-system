package isel.openapi.mock.repository

import isel.openapi.mock.domain.dynamic.ResponseInfo
import isel.openapi.mock.domain.dynamic.ScenarioInfo
import isel.openapi.mock.domain.dynamic.SpecAndScenario
import isel.openapi.mock.domain.openAPI.PathOperations
import isel.openapi.mock.domain.openAPI.SpecInfo
import isel.openapi.mock.repository.jdbi.JdbiOpenAPIRepository
import isel.openapi.mock.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import kotlin.test.assertEquals

class JdbiOpenAPIRepositoryTest {

    @Test
    fun `can upload OpenAPI`() {
        runWithHandle { handle ->
            val repo = JdbiOpenAPIRepository(handle)
            val result = repo.uploadOpenAPI()

            assertEquals(expected[0].host, result[0].host)
            assertEquals(expected[0].spec.name, result[0].spec.name)
            assertEquals(expected[0].spec.description, result[0].spec.description)
            assertEquals(expected[0].spec.paths.size, result[0].spec.paths.size)
            assertEquals(expected[0].spec.paths[0].path, result[0].spec.paths[0].path)
            assertEquals(expected[0].spec.paths[0].operations, result[0].spec.paths[0].operations)
            assertEquals(expected[0].scenarios.size, result[0].scenarios.size)
            assertEquals(expected[0].scenarios[0].name, result[0].scenarios[0].name)
            assertEquals(expected[0].scenarios[0].method, result[0].scenarios[0].method)
            assertEquals(expected[0].scenarios[0].path, result[0].scenarios[0].path)
            assertEquals(expected[0].scenarios[0].responses.size, result[0].scenarios[0].responses.size)
            assertEquals(expected[0].scenarios[0].responses[0].statusCode, result[0].scenarios[0].responses[0].statusCode)
            assertEquals(expected[0].scenarios[0].responses[0].contentType, result[0].scenarios[0].responses[0].contentType)
            assertEquals(
                expected[0].scenarios[0].responses[0].headers,
                result[0].scenarios[0].responses[0].headers
            )
            assertEquals(
                expected[0].scenarios[0].responses[0].body!!.decodeToString(),
                result[0].scenarios[0].responses[0].body!!.decodeToString()
            )
            assertEquals(expected.size, result.size)
        }
    }

    val expected = listOf(
        SpecAndScenario(
            SpecInfo(
                name = "ChIMP API",
                description = "API for Instant messaging application",
                paths = listOf(
                    PathOperations(
                        "/users/search",
                        "[{\"method\": \"GET\", \"headers\": [], \"servers\": [], \"security\": true, \"responses\": [{\"schema\": {\"@type\": \"ContentField\", \"content\": {\"application/json\": {\"@type\": \"SchemaObject\", \"schema\": \"{\\\"type\\\":\\\"array\\\",\\\"exampleSetFlag\\\":false,\\\"items\\\":{\\\"type\\\":\\\"object\\\",\\\"properties\\\":{\\\"id\\\":{\\\"type\\\":\\\"integer\\\",\\\"example\\\":1,\\\"exampleSetFlag\\\":true,\\\"types\\\":[\\\"integer\\\"]},\\\"username\\\":{\\\"type\\\":\\\"string\\\",\\\"example\\\":\\\"bob123\\\",\\\"exampleSetFlag\\\":true,\\\"types\\\":[\\\"string\\\"]}},\\\"exampleSetFlag\\\":false,\\\"types\\\":[\\\"object\\\"]},\\\"types\\\":[\\\"array\\\"]}\"}}}, \"headers\": [{\"name\": \"A\", \"type\": {\"@type\": \"SchemaObject\", \"schema\": \"{\\\"type\\\":\\\"string\\\",\\\"exampleSetFlag\\\":false,\\\"types\\\":[\\\"string\\\"]}\"}, \"style\": \"FORM\", \"explode\": false, \"required\": false, \"description\": \"bom dia\"}], \"statusCode\": \"OK\"}, {\"schema\": null, \"headers\": [], \"statusCode\": \"INTERNAL_SERVER_ERROR\"}], \"parameters\": [{\"name\": \"username\", \"type\": {\"@type\": \"SchemaObject\", \"schema\": \"{\\\"type\\\":\\\"string\\\",\\\"exampleSetFlag\\\":false,\\\"types\\\":[\\\"string\\\"]}\"}, \"style\": \"FORM\", \"explode\": true, \"location\": \"QUERY\", \"required\": true, \"description\": null, \"allowEmptyValue\": false}, {\"name\": \"limit\", \"type\": {\"@type\": \"SchemaObject\", \"schema\": \"{\\\"type\\\":\\\"integer\\\",\\\"exampleSetFlag\\\":false,\\\"types\\\":[\\\"integer\\\"]}\"}, \"style\": \"FORM\", \"explode\": true, \"location\": \"QUERY\", \"required\": false, \"description\": null, \"allowEmptyValue\": false}, {\"name\": \"skip\", \"type\": {\"@type\": \"SchemaObject\", \"schema\": \"{\\\"type\\\":\\\"integer\\\",\\\"exampleSetFlag\\\":false,\\\"types\\\":[\\\"integer\\\"]}\"}, \"style\": \"FORM\", \"explode\": true, \"location\": \"QUERY\", \"required\": false, \"description\": null, \"allowEmptyValue\": false}], \"requestBody\": {\"content\": {\"@type\": \"ContentField\", \"content\": {}}, \"required\": false}}]"
                    ),
                    PathOperations(
                        "/users/{id}",
                        "[\n  {\n    \"method\": \"GET\",\n    \"headers\": [],\n    \"servers\": [\"http://localhost:8080/api\"],\n    \"security\": false,\n    \"responses\": [\n      {\n        \"schema\": {\n          \"@type\": \"ContentField\",\n          \"content\": {\n            \"application/json\": {\n              \"@type\": \"SchemaObject\",\n              \"schema\": \"{ \\\"type\\\": \\\"object\\\", \\\"properties\\\": { \\\"id\\\": { \\\"type\\\": \\\"integer\\\" }, \\\"username\\\": { \\\"type\\\": \\\"string\\\" } } }\"\n            }\n          }\n        },\n        \"headers\": [],\n        \"statusCode\": \"200\"\n      },\n      {\n        \"schema\": {\n          \"@type\": \"ContentField\",\n          \"content\": {\n            \"application/json\": {\n              \"@type\": \"SchemaObject\",\n              \"schema\": \"{ \\\"type\\\": \\\"string\\\", \\\"example\\\": \\\"User not found\\\" }\"\n            }\n          }\n        },\n        \"headers\": [],\n        \"statusCode\": \"404\"\n      },\n      {\n        \"schema\": null,\n        \"headers\": [],\n        \"statusCode\": \"500\"\n      }\n    ],\n    \"parameters\": [\n      {\n        \"name\": \"id\",\n        \"type\": {\n          \"@type\": \"SchemaObject\",\n          \"schema\": \"{ \\\"type\\\": \\\"integer\\\" }\"\n        },\n        \"style\": \"SIMPLE\",\n        \"explode\": false,\n        \"location\": \"PATH\",\n        \"required\": true,\n        \"description\": \"The ID of the user\",\n        \"allowEmptyValue\": false\n      }\n    ],\n    \"requestBody\": {\n      \"content\": {\n        \"@type\": \"ContentField\",\n        \"content\": {}\n      },\n      \"required\": false\n    }\n  }\n]"
                )
                )
            ),
            scenarios = listOf(
                ScenarioInfo(
                    name = "test1",
                    method = "GET",
                    path = "/users/search",
                    responses = listOf(
                        ResponseInfo(
                            statusCode = "200",
                            contentType =  "application/json",
                            headers = "{\"A\": \"bom dia\"}",
                            body = "[{\"id\":5,\"username\":\"diogo\"},{\"id\":40,\"username\":\"martim\"}]".toByteArray()
                        )
                    )
                )
            ),
            "host1"
        )
    )

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) =
            jdbi.useTransaction<Exception>(block)

        private val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL("jdbc:postgresql://localhost:5435/mock?user=mock&password=mock")
            }
        ).configureWithAppRequirements()
    }
    
}