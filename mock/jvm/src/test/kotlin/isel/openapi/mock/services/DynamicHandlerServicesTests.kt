package isel.openapi.mock.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jknack.handlebars.Handlebars
import isel.openapi.mock.domain.dynamic.DynamicDomain
import isel.openapi.mock.domain.dynamic.ProcessedRequest
import isel.openapi.mock.domain.openAPI.ApiParameter
import isel.openapi.mock.domain.openAPI.ApiPath
import isel.openapi.mock.domain.openAPI.ApiRequestBody
import isel.openapi.mock.domain.openAPI.ApiSpec
import isel.openapi.mock.domain.openAPI.ContentOrSchema
import isel.openapi.mock.domain.openAPI.HttpMethod
import isel.openapi.mock.domain.openAPI.Location
import isel.openapi.mock.domain.openAPI.ParameterStyle
import isel.openapi.mock.domain.openAPI.PathOperation
import isel.openapi.mock.domain.openAPI.PathParts
import isel.openapi.mock.domain.openAPI.Response
import isel.openapi.mock.domain.openAPI.StatusCode
import isel.openapi.mock.domain.problems.ParameterInfo
import isel.openapi.mock.domain.problems.ProblemsDomain
import isel.openapi.mock.repository.DynamicRoutesRepository
import isel.openapi.mock.repository.jdbi.JdbiTransactionManager
import isel.openapi.mock.repository.jdbi.configureWithAppRequirements
import isel.openapi.mock.utils.Failure
import isel.openapi.mock.utils.Success
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.assertEquals

class DynamicHandlerServicesTests {

    @Test
    fun `can execute Dynamic Handler`() {

        val router = Router(
            repository = DynamicRoutesRepository(),
            dynamicDomain = DynamicDomain()
        )

        val dynamicServices = createDynamicServices(router)

        val node = router.createRouterNode(apiSpec, listOf(scenario))
        router.register(mapOf("host1" to node))

        val result = dynamicServices.executeDynamicHandler(
            host = "host1",
            method = HttpMethod.GET,
            path = "/users/1",
            request = MockHttpServletRequest("GET", "/users/1"),
            externalKey = null,
        )

        assert(result is Success) { "Expected success but got failure: $result" }
        val response = (result as Success).value

        assertEquals(StatusCode.fromCode("500"), response.statusCode)
        assertEquals(null, response.contentType)
        assertEquals(emptyMap<String, String>(), response.headers)
        assertEquals(null, response.body)

    }

    @Test
    fun `scenario with multiple responses returns correct response`() {
        val router = Router(
            repository = DynamicRoutesRepository(),
            dynamicDomain = DynamicDomain()
        )

        val dynamicServices = createDynamicServices(router)

        val node = router.createRouterNode(apiSpec, listOf(scenario))
        router.register(mapOf("host1" to node))

        val results = mutableListOf<Success<ProcessedRequest>>()

        // Simulate multiple requests to the same scenario
        for (i in 1..4) {
            val result = dynamicServices.executeDynamicHandler(
                host = "host1",
                method = HttpMethod.GET,
                path = "/users/1",
                request = MockHttpServletRequest("GET", "/users/1"),
                externalKey = null,
            )
            if (result is Success) {
                results.add(result)
            } else {
                throw Exception("Expected success but got failure: $result")
            }
        }

        assertEquals(4, results.size)
        assertEquals(StatusCode.fromCode("500"), results[0].value.statusCode)
        assertEquals(null, results[0].value.contentType)
        assertEquals(emptyMap<String, String>(), results[0].value.headers)
        assertEquals(null, results[0].value.body)
        assertEquals(StatusCode.fromCode("404"), results[1].value.statusCode)
        assertEquals("application/json", results[1].value.contentType)
        assertEquals(emptyMap<String, String>(), results[1].value.headers)
        assertEquals("""{"error": "User not found"}""", results[1].value.body)
        assertEquals(StatusCode.fromCode("200"), results[2].value.statusCode)
        assertEquals("application/json", results[2].value.contentType)
        assertEquals(emptyMap<String, String>(), results[2].value.headers)
        assertEquals("""{"id": 1, "username": "bob123"}""", results[2].value.body)
        assertEquals(StatusCode.fromCode("500"), results[3].value.statusCode)
    }

    @Test
    fun `failed to a non-existent scenario`() {
        val router = Router(
            repository = DynamicRoutesRepository(),
            dynamicDomain = DynamicDomain()
        )

        val dynamicServices = createDynamicServices(router)

        val node = router.createRouterNode(apiSpec, listOf())
        router.register(mapOf("host1" to node))

        val request = MockHttpServletRequest("GET", "/users/search")
        request.queryString = "username=bob"
        request.addHeader("Authorization", "Bearer Token123456789012345678901234567890")

        val result = dynamicServices.executeDynamicHandler(
            host = "host1",
            method = HttpMethod.GET,
            path = "/users/search",
            request = request,
            externalKey = null,
        )

        assert(result is Failure)
        val error = result as Failure<DynamicHandlerError>
        assertEquals(DynamicHandlerError.ScenarioNotFound, error.value)
    }

    @Test
    fun `host does not exist`() {
        val router = Router(
            repository = DynamicRoutesRepository(),
            dynamicDomain = DynamicDomain()
        )

        val dynamicServices = createDynamicServices(router)

        router.createRouterNode(apiSpec, listOf(scenario))

        val result = dynamicServices.executeDynamicHandler(
            host = "nonExistentHost",
            method = HttpMethod.GET,
            path = "/users/1",
            request = MockHttpServletRequest("GET", "/users/1"),
            externalKey = null,
        )

        assert(result is Failure)
        val error = result as Failure<DynamicHandlerError>
        assertEquals(DynamicHandlerError.HostDoesNotExist, error.value)
    }

    @Test
    fun `handler not found for given path and method`() {
        val router = Router(
            repository = DynamicRoutesRepository(),
            dynamicDomain = DynamicDomain()
        )

        val dynamicServices = createDynamicServices(router)

        val node = router.createRouterNode(apiSpec, listOf(scenario))
        router.register(mapOf("host1" to node))

        val result = dynamicServices.executeDynamicHandler(
            host = "host1",
            method = HttpMethod.POST, // Using a method that does not match the scenario
            path = "/users/1",
            request = MockHttpServletRequest("POST", "/users/1"),
            externalKey = null,
        )

        assert(result is Failure)
        val error = result as Failure<DynamicHandlerError>
        assertEquals(DynamicHandlerError.HandlerNotFound, error.value)
    }

    @Test
    fun `bad request with invalid parameters`() {
        val router = Router(
            repository = DynamicRoutesRepository(),
            dynamicDomain = DynamicDomain()
        )

        val dynamicServices = createDynamicServices(router)

        val node = router.createRouterNode(apiSpec, listOf(scenario))
        router.register(mapOf("host1" to node))

        val request = MockHttpServletRequest("GET", "/users/1")
        request.addParameter("invalidParam", "value")

        val result = dynamicServices.executeDynamicHandler(
            host = "host1",
            method = HttpMethod.GET,
            path = "/users/1",
            request = request,
            externalKey = null,
        )

        assert(result is Failure)
        val error = result as Failure<DynamicHandlerError>
        val exchangeKey = (error as Failure<DynamicHandlerError.BadRequest>).value.exchangeKey
        assertEquals(DynamicHandlerError.BadRequest(exchangeKey), error.value)
    }

    @Test
    fun `can update Dynamic Router`() {

        val router = Router(
            repository = DynamicRoutesRepository(),
            dynamicDomain = DynamicDomain()
        )

        val dynamicServices = createDynamicServices(router)

        dynamicServices.updateDynamicRouter()

        val match = router.match("host1", HttpMethod.GET, "/users/search")

        assert(match != null)
        assertEquals("/users/search", match!!.pathTemplate)
        assertEquals(HttpMethod.GET, match.routeNode.operations.first().method)
    }

    val scenario = Scenario(
        name = "test1",
        method = HttpMethod.GET,
        path = "/users/{id}",
        responses = listOf(
            ResponseConfig(
                statusCode = StatusCode.fromCode("500")!!,
                contentType = null,
                headers = null,
                body = null
            ),
            ResponseConfig(
                statusCode = StatusCode.fromCode("404")!!,
                contentType = "application/json",
                headers = null,
                body = """{"error": "User not found"}""".toByteArray()
            ),
            ResponseConfig(
                statusCode = StatusCode.fromCode("200")!!,
                contentType = "application/json",
                headers = null,
                body = """{"id": 1, "username": "bob123"}""".toByteArray()
            )
        )
    )

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

    @Test
    fun `can process template body with handlebars context`() {
        val handlebars = Handlebars()
        val mapper = jacksonObjectMapper()
        val context = HandlebarsContext()
            .addParams(
                listOf(
                    ParameterInfo(
                        name = "name",
                        content = "John",
                        location = Location.QUERY,
                        type = ContentOrSchema.SchemaObject("""{ "type": "string" }"""),
                    ),
                    ParameterInfo(
                        name = "name",
                        content = "Anna",
                        location = Location.QUERY,
                        type = ContentOrSchema.SchemaObject("""{ "type": "string" }"""),
                    ),
                    ParameterInfo(
                        name = "name",
                        content = "Kevin",
                        location = Location.QUERY,
                        type = ContentOrSchema.SchemaObject("""{ "type": "string" }"""),
                    )
                )
            )
            .getContext()

        val template = """
            {{#each queryParams.name}} { "name": "{{this}}" }{{#if @last}} {{else}},{{/if}} {{/each}}
        """.trimIndent()

        val compiledTemplate = handlebars.compileInline(template)
        val result = compiledTemplate.apply(context)
        val parsed = mapper.readTree(result)
        val parsedResult = mapper.readTree("""
            {
                "name": "John"
            },
            {
                "name": "Anna"
            },
            {
                "name": "Kevin"
            },
            """.trimIndent())

        assertEquals(
            parsedResult,
            parsed
        )
    }

    @Test
    fun `can process template body with handlebars context and path parts`() {
        val handlebars = Handlebars()
        val mapper = jacksonObjectMapper()
        val context = HandlebarsContext()
            .addParams(
                listOf(
                    ParameterInfo(
                        name = "id",
                        content = "123",
                        location = Location.PATH,
                        type = ContentOrSchema.SchemaObject("""{ "type": "integer" }"""),
                    )
                )
            )
            .pathParts("/users/123")
            .getContext()

        val template = """
            { "userId": "{{pathParts.0}}" }
        """.trimIndent()

        val compiledTemplate = handlebars.compileInline(template)
        val result = compiledTemplate.apply(context)
        val parsed = mapper.readTree(result)

        assertEquals(
            mapper.readTree("""{ "userId": "users" }"""),
            parsed
        )
    }

    @Test
    fun `can process template body with handlebars context 1`() {
        val handlebars = Handlebars()
        val mapper = jacksonObjectMapper()
        val context = HandlebarsContext()
            .addParams(
                listOf(
                    ParameterInfo(
                        name = "name",
                        content = "John",
                        location = Location.PATH,
                        type = ContentOrSchema.SchemaObject("""{ "type": "string" }"""),
                    ),
                    ParameterInfo(
                        name = "name",
                        content = "Anna",
                        location = Location.PATH,
                        type = ContentOrSchema.SchemaObject("""{ "type": "string" }"""),
                    ),
                    ParameterInfo(
                        name = "name",
                        content = "Kevin",
                        location = Location.PATH,
                        type = ContentOrSchema.SchemaObject("""{ "type": "string" }"""),
                    )
                )
            )
            .getContext()

        val template = """
            {{#each pathParams.name}} { "name": "{{this}}" }{{#if @last}} {{else}},{{/if}} {{/each}}
        """.trimIndent()

        val compiledTemplate = handlebars.compileInline(template)
        val result = compiledTemplate.apply(context)
        val parsed = mapper.readTree(result)
        val parsedResult = mapper.readTree("""
            {
                "name": "John"
            },
            {
                "name": "Anna"
            },
            {
                "name": "Kevin"
            },
            """.trimIndent())

        assertEquals(
            parsedResult,
            parsed
        )
    }

    companion object {
        private val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL("jdbc:postgresql://localhost:5435/mock?user=mock&password=mock")
            }
        ).configureWithAppRequirements()

        private fun createDynamicServices(router: Router): DynamicHandlerServices {
            return DynamicHandlerServices(
                router = router,
                problemsDomain = ProblemsDomain(),
                transactionManager = JdbiTransactionManager(jdbi),
                clock = Clock.System,
                handlebars = Handlebars()
            )
        }
    }

}