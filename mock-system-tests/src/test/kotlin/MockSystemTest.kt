import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.context.ConfigurableApplicationContext
import kotlin.test.Test
import isel.openapi.mock.MockApplication
import isel.openapi.admin.AdminApplication
import org.junit.jupiter.api.TestInstance
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

// Para garantir que os métodos com as anotações @BeforeAll e @AfterAll sejam executados corretamente. Mantém estado para todos os testes na mesma instância da classe de teste.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MockSystemTest {

    private lateinit var adminContext: ConfigurableApplicationContext
    private lateinit var mockContext: ConfigurableApplicationContext

    @BeforeAll // Executa antes de todos os testes da classe, apenas uma vez.
    fun startApps(): Unit {
        mockContext = SpringApplication(MockApplication::class.java).apply {
            setDefaultProperties(mapOf("server.port" to "8082"))
            webApplicationType = WebApplicationType.SERVLET
        }.run()

        adminContext = SpringApplication(AdminApplication::class.java).apply {
            setDefaultProperties(mapOf("server.port" to "8081"))
            webApplicationType = WebApplicationType.SERVLET
        }.run()
    }

    @AfterAll // Executa depois de todos os testes da classe estarem concluídos, apenas uma vez.
    fun stopApps(): Unit {
        adminContext.close()
        mockContext.close()
    }

    @Test
    fun `App1 can call App2`() {
        val restTemplate = RestTemplate()

        val response = restTemplate.postForObject(
            "http://localhost:8081/admin/openapi",
            mapOf("spec" to "openapi: 3.0.0\ninfo:\n  title: Test API\n  version: 1.0.0"),
            String::class.java
        )

        assertEquals(null, response)
    }
}