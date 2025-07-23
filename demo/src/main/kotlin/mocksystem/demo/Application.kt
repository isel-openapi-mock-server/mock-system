package mocksystem.demo

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json
import mocksystem.demo.services.Twilio
import mocksystem.demo.services.TwilioMock

@SpringBootApplication
class Application {

    @Bean
    fun twilioMock(): Twilio =
        TwilioMock(
            client = HttpClient {
                install(ContentNegotiation) { // enables automatic serialization and deserialization of req and resp bodies.
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        }
                    ) // faz o uso do Jackson para serialização e desserialização de JSON
                }
            },
            host = "demohost.mocksystem.com",
        )
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
