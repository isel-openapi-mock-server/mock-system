package mocksystem.demo

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
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
                    json() // faz o uso do Jackson para serialização e desserialização de JSON
                }
            },
            host = "localhost:8081"
        )
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
