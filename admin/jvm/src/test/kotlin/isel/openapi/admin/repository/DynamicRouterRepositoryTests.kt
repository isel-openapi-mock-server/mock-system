package isel.openapi.admin.repository

import isel.openapi.admin.domain.admin.AdminDomain
import isel.openapi.admin.domain.admin.RouteNode
import kotlin.test.Test
import kotlin.test.assertEquals


class DynamicRouterRepositoryTests {

    private val adminDomain = AdminDomain()
    private val resolverRepository = ResolverRepository()

    @Test
    fun `register, match and remove test`() {
        val transactionToken = adminDomain.generateTokenValue()

        val node = RouteNode(part = "")

        resolverRepository.register(transactionToken, node)

        val getNode = resolverRepository.getValidator(transactionToken)

        assertEquals(node, getNode)

        resolverRepository.remove(transactionToken)

        val getNode2 = resolverRepository.getValidator(transactionToken)

        assertEquals(null, getNode2)
    }

}