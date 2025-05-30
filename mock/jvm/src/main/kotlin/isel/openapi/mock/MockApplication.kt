package isel.openapi.mock

import isel.openapi.mock.repository.PostgresListener
import isel.openapi.mock.repository.jdbi.configureWithAppRequirements
import isel.openapi.mock.services.DynamicHandlerServices
import isel.openapi.mock.services.Synchronizer
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

@SpringBootApplication
class MockApplication {

	@Bean
	fun dataSource() =
		PGSimpleDataSource().apply {
			setURL(Environment.getDbUrl())
		}

	@Bean
	fun jdbi(dataSource: DataSource) =
		Jdbi.create(
			dataSource,
		).configureWithAppRequirements()

}

fun main(args: Array<String>) {
	runApplication<MockApplication>(*args)
}
