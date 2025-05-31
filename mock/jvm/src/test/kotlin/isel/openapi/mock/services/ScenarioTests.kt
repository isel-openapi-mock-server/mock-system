package isel.openapi.mock.services

import isel.openapi.mock.domain.openAPI.*
import kotlin.test.Test

class ScenarioTests {

    val scenario = Scenario(
        name = "TestScenario",
        method = HttpMethod.GET,
        path = "/test/{id}",
        responses = listOf(
            ResponseConfig(
                statusCode = StatusCode.fromCode("500")!!,
                contentType = "application/json",
                headers = null,
                body = """{"message": "error"}""".toByteArray() 
            ),
            ResponseConfig(
                statusCode = StatusCode.fromCode("200")!!,
                contentType = "application/json",
                headers = mapOf("X-Test-Header" to "TestValue"),
                body = """{"message": "Success"}""".toByteArray()
            )
        )
    )

    @Test
    fun `test scenario response cycling`() {
        // First call should return the first response
        val firstResponse = scenario.getResponse()
        assert(firstResponse.statusCode == StatusCode.fromCode("500"))
        assert(String(firstResponse.body ?: ByteArray(0)) == """{"message": "error"}""")

        // Second call should return the second response
        val secondResponse = scenario.getResponse()
        assert(secondResponse.statusCode == StatusCode.fromCode("200"))
        assert(String(secondResponse.body ?: ByteArray(0)) == """{"message": "Success"}""")

        // Third call should cycle back to the first response
        val thirdResponse = scenario.getResponse()
        assert(thirdResponse.statusCode == StatusCode.fromCode("500"))
    }

}