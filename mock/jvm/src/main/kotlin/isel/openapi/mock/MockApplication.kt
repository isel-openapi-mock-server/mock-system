package isel.openapi.mock

import com.github.jknack.handlebars.Handlebars
import isel.openapi.mock.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import kotlinx.datetime.Clock
import javax.sql.DataSource

@SpringBootApplication
class MockApplication {

	@Bean
	fun clock() = Clock.System

	@Bean
	fun handlebars() = Handlebars()

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
