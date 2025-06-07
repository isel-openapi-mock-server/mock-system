package isel.openapi.admin.services

import isel.openapi.admin.repository.jdbi.JdbiTransactionManager
import isel.openapi.admin.repository.jdbi.configureWithAppRequirements
import isel.openapi.admin.utils.Failure
import isel.openapi.admin.utils.Success
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestsServicesTests {


    @Test
    fun `getRequestInfo should detect missing request credentials`() {

        val result = requestsServices.getRequestInfo(null, null)

        assertTrue(result is Failure)

        assertEquals(RequestInfoError.RequestCredentialIsRequired, result.value)

    }

    @Test
    fun `getRequestInfo should detect missing request doesn't exist`() {

        val result = requestsServices.getRequestInfo("banana", null)

        assertTrue(result is Failure)

        assertEquals(RequestInfoError.RequestNotFound, result.value)

    }

    @Test
    fun `getRequestInfo should return success with exchangeKey`() {

        val result = requestsServices.getRequestInfo("request1", null)

        assertTrue(result is Success)

        val reqInfoList = result.value

        assertEquals(1, reqInfoList.size)

        assertEquals("/users/search", reqInfoList.first().path)
    }

    @Test
    fun `getRequestInfo should return success with externalKey`() {

        val result = requestsServices.getRequestInfo(null, "type2")

        assertTrue(result is Success)

        val reqInfoList = result.value

        assertEquals(2, reqInfoList.size)

        assertEquals("request1", reqInfoList[0].exchangeKey)
        assertEquals("request2", reqInfoList[1].exchangeKey)
    }


    companion object {
        private val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL("jdbc:postgresql://localhost:5434/admin?user=mock&password=mock")
            }
        ).configureWithAppRequirements()

        private val requestsServices = RequestsServices(transactionManager = JdbiTransactionManager(jdbi))
    }
}