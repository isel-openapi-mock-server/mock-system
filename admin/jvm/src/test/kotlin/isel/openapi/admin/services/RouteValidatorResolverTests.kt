package isel.openapi.admin.services

import isel.openapi.admin.domain.admin.AdminDomain
import isel.openapi.admin.parsing.model.*
import isel.openapi.admin.repository.ResolverRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteValidatorResolverTests {

    private val resolverRepository = ResolverRepository()

    private val adminDomain = AdminDomain()

    private val routeValidatorResolver = RouteValidatorResolver(
        resolverRepository,
        adminDomain
    )

    @Test
    fun registerTest() {
        val spec = ApiSpec(
            name = "marco",
            description = "",
            paths = listOf(
                ApiPath(
                    fullPath = "/a",
                    path = listOf(PathParts("a", false)),
                    operations = listOf(
                        PathOperation(
                            method = HttpMethod.GET,
                            security = false,
                            parameters = emptyList(),
                            requestBody = null,
                            responses = emptyList(),
                            servers = emptyList(),
                            headers = emptyList()
                        )
                    )
                )
            )
        )

        val transactionToken = adminDomain.generateTokenValue()

        routeValidatorResolver.register(spec, transactionToken)

        val routeNode = resolverRepository.getValidator(transactionToken)

        assertTrue(routeNode != null)

        val aNode = routeNode.children["a"]

        assertTrue(aNode != null)

        assertEquals(emptyMap(), aNode.children)
        assertEquals("a", aNode.part)
        assertEquals(false, aNode.isParameter)
        assertEquals(1, aNode.validators.size)
    }

    @Test
    fun matchTest() {
        val spec = ApiSpec(
            name = "marco",
            description = "",
            paths = listOf(
                ApiPath(
                    fullPath = "/a",
                    path = listOf(PathParts("a", false)),
                    operations = listOf(
                        PathOperation(
                            method = HttpMethod.GET,
                            security = false,
                            parameters = emptyList(),
                            requestBody = null,
                            responses = emptyList(),
                            servers = emptyList(),
                            headers = emptyList()
                        )
                    )
                )
            )
        )

        val transactionToken = adminDomain.generateTokenValue()

        routeValidatorResolver.register(spec, transactionToken)

        val respVal = routeValidatorResolver.match(
            transactionToken = transactionToken,
            method = HttpMethod.GET,
            path = "/a")

        assertTrue(respVal != null)
    }

    @Test
    fun removeTest() {
        val spec = ApiSpec(
            name = "marco",
            description = "",
            paths = listOf(
                ApiPath(
                    fullPath = "/a",
                    path = listOf(PathParts("a", false)),
                    operations = listOf(
                        PathOperation(
                            method = HttpMethod.GET,
                            security = false,
                            parameters = emptyList(),
                            requestBody = null,
                            responses = emptyList(),
                            servers = emptyList(),
                            headers = emptyList()
                        )
                    )
                )
            )
        )

        val transactionToken = adminDomain.generateTokenValue()

        routeValidatorResolver.register(spec, transactionToken)

        val routeNode = resolverRepository.getValidator(transactionToken)

        assertTrue(routeNode != null)

        routeValidatorResolver.remove(transactionToken)

        val routeNode2 = resolverRepository.getValidator(transactionToken)

        assertTrue(routeNode2 == null)

    }



}