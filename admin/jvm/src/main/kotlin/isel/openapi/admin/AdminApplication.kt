package isel.openapi.admin

import isel.openapi.admin.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class AdminApplication {

	@Bean
	fun jdbi() =
		Jdbi.create(
			PGSimpleDataSource().apply {
				setURL(Environment.getDbUrl())
			},
		).configureWithAppRequirements()

}

fun main(args: Array<String>) {
	runApplication<AdminApplication>(*args)
}
