package isel.openapi.mock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@SpringBootApplication
class MockApplication {

}

fun main(args: Array<String>) {
	runApplication<MockApplication>(*args)
}
