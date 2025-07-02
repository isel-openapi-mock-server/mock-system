package isel.openapi.mock.repository

import isel.openapi.mock.services.DynamicHandlerServices
import isel.openapi.mock.services.Synchronizer
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.postgresql.PGConnection
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.sql.DataSource
import kotlin.concurrent.thread

@Component
class PostgresListener(
    private val dataSource: DataSource,
    private val synchronizer: Synchronizer,
) {

    private lateinit var thread: Thread

    @PostConstruct
    fun startListening() {
        val conn = dataSource.connection
        val pgConn = conn.unwrap(PGConnection::class.java)
        conn.createStatement().use {
            it.execute("LISTEN update_spec")
        }
        thread = Thread.ofPlatform().start {
            while (true) {
                try {
                    val notifications = pgConn.notifications
                    if (notifications != null) {
                        for (notification in notifications) {
                            if(notification.name == "update_spec") {
                                println("Received notification: ${notification.name}")
                                synchronizer.queue()
                            }
                        }
                    }
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    logger.info("Thread interrupted, stopping PostgresListener.")
                    Thread.currentThread().interrupt()
                    break
                }

            }
        }

        thread.start()
    }

    @PreDestroy
    fun destroy() {
        logger.info("Stopping PostgresListener thread.")
        thread.interrupt()
        logger.info("Waiting for PostgresListener thread to finish.")
        thread.join()
        logger.info("PostgresListener thread stopped.")
    }

    companion object {
        private val logger = LoggerFactory.getLogger("PostgresListener")
    }
}