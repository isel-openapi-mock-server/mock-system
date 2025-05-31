package isel.openapi.mock.services

import isel.openapi.mock.domain.dynamic.DynamicDomain
import isel.openapi.mock.domain.openAPI.*
import isel.openapi.mock.repository.DynamicRoutesRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RouterTests {

    val router = Router(DynamicRoutesRepository(), DynamicDomain())

    @Test
    fun `create router node`() {

        val node = router.createRouterNode(apiSpec, emptyList())

        assert(node.part == "")
        assert(node.children.size == 1)
        assert(node.children["users"] != null)
        assert(node.children["users"]!!.children.size == 1)
        assert(node.children["users"]!!.children["{id}"] != null)
        assert(node.children["users"]!!.part == "users")
        assert(node.children["users"]!!.operations.size == 1)
        assert(node.children["users"]!!.operations.first().method == HttpMethod.POST)
        assert(node.children["users"]!!.operations.first().fullPath == "/users")
        assert(node.children["users"]!!.operations.first().scenariosNames.isEmpty())

    }

    @Test
    fun `register and match router node`() {

        val getScenario = Scenario(
            name = "GetUserScenario",
            method = HttpMethod.GET,
            path = "/users/{id}",
            responses = listOf(
                ResponseConfig(
                    StatusCode.fromCode("200")!!,
                    "application/json",
                    null,
                    "{ \"id\": 1, \"username\": \"bob123\" }".toByteArray()
                )
            )
        )

        val node = router.createRouterNode(apiSpec, listOf(getScenario))
        val host = "host123"

        router.register(mapOf(host to node))

        val matchedNode1 = router.match(host, HttpMethod.POST, "/users")

        assert(matchedNode1 != null)
        assert(matchedNode1!!.routeNode.part == "users")
        assert(matchedNode1.routeNode.operations.size == 1)
        assert(matchedNode1.routeNode.operations.first().method == HttpMethod.POST)
        assert(matchedNode1.routeNode.operations.first().fullPath == "/users")

        val matchedNode2 = router.match(host, HttpMethod.GET, "/users")

        assert(matchedNode2 == null)

        assertTrue { router.doesHostExist(host) }
        assertFalse { router.doesHostExist("nonexistentHost") }

        val matchedNode = router.match(host, HttpMethod.GET, "/users/1")

        assert(matchedNode != null)
        assert(matchedNode!!.routeNode.part == "{id}")
        assert(matchedNode.routeNode.operations.size == 1)
        assert(matchedNode.routeNode.operations.first().method == HttpMethod.GET)
        assert(matchedNode.routeNode.operations.first().fullPath == "/users/{id}")
        assert(matchedNode.routeNode.operations.first().scenariosNames.contains(getScenario.name))
        assert(matchedNode.resourceUrl == "/users/{id}")

        assertTrue(router.doesScenarioExist(
            matchedNode.routeNode,
            getScenario.name,
            "/users/{id}",
            HttpMethod.GET
        ))

    }

    val apiSpec = ApiSpec(
        name = "ChIMP API",
        description = "API for Instant messaging application",
        paths = listOf(
            ApiPath(
                fullPath = "/users",
                path = listOf(PathParts("users", isParam = false)),
                operations = listOf(
                    PathOperation(
                        method = HttpMethod.POST,
                        security = false,
                        parameters = emptyList(),
                        requestBody = ApiRequestBody(
                            content = ContentOrSchema.ContentField(
                                content = mapOf(
                                    "application/json" to ContentOrSchema.SchemaObject(
                                        schema = """
                                    {
                                        "type": "object",
                                        "properties": {
                                            "inviteCode": { "type": "string", "example": "jFeqtSelG7Nj" },
                                            "username": { "type": "string", "example": "bob123" },
                                            "password": { "type": "string", "example": "password123" }
                                        }
                                    }
                                    """.trimIndent()
                                    )
                                )
                            ),
                            required = true
                        ),
                        responses = listOf(
                            Response(StatusCode.fromCode("201")!!, null),
                            Response(
                                StatusCode.fromCode("400")!!,
                                ContentOrSchema.SchemaObject(
                                    """
                                {
                                    "type": "string",
                                    "enum": [
                                        "User already exists",
                                        "Password is insecure",
                                        "Invalid username",
                                        "Invalid register code"
                                    ],
                                    "example": "User already exists"
                                }
                                """.trimIndent()
                                )
                            ),
                            Response(StatusCode.fromCode("500")!!, null)
                        ),
                        servers = listOf("http://localhost:8080/api"),
                        headers = emptyList()
                    )
                )
            ),
            ApiPath(
                fullPath = "/users/{id}",
                path = listOf(
                    PathParts("users", isParam = false),
                    PathParts("id", isParam = true)
                ),
                operations = listOf(
                    PathOperation(
                        method = HttpMethod.GET,
                        security = false,
                        parameters = listOf(
                            ApiParameter(
                                name = "id",
                                location = Location.PATH,
                                description = "The ID of the user",
                                type = ContentOrSchema.SchemaObject("""{ "type": "integer" }"""),
                                required = true,
                                allowEmptyValue = false,
                                style = ParameterStyle.SIMPLE,
                                explode = false
                            )
                        ),
                        requestBody = null,
                        responses = listOf(
                            Response(
                                StatusCode.fromCode("200")!!,
                                ContentOrSchema.SchemaObject(
                                    """
                                {
                                    "type": "object",
                                    "properties": {
                                        "id": { "type": "integer" },
                                        "username": { "type": "string" }
                                    }
                                }
                                """.trimIndent()
                                )
                            ),
                            Response(StatusCode.fromCode("404")!!, ContentOrSchema.SchemaObject("""{ "type": "string", "example": "User not found" }""")),
                            Response(StatusCode.fromCode("500")!!, null)
                        ),
                        servers = listOf("http://localhost:8080/api"),
                        headers = emptyList()
                    )
                )
            )
        )
    )

}