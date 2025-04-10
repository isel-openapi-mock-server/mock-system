package isel.openapi.mock

import isel.openapi.mock.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@SpringBootApplication
class MockApplication {

	@Bean
	fun jdbi() =
		Jdbi.create(
			PGSimpleDataSource().apply {
				setURL(Environment.getDbUrl())
			},
		).configureWithAppRequirements()

}

fun main(args: Array<String>) {
	runApplication<MockApplication>(*args)
}
