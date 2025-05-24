package isel.openapi.mock.repository

import isel.openapi.mock.services.DynamicHandlerServices
import jakarta.annotation.PostConstruct
import org.postgresql.PGConnection
import org.springframework.stereotype.Component
import javax.sql.DataSource
import kotlin.concurrent.thread

@Component
class PostgresListener(
    private val dataSource: DataSource,
    private val dynamicHandlerServices: DynamicHandlerServices
) {

    @PostConstruct
    fun startListening() {
        val conn = dataSource.connection
        val pgConn = conn.unwrap(PGConnection::class.java)
        conn.createStatement().use {
            it.execute("LISTEN update_spec")
        }
        Thread.startVirtualThread {
            while (true) {
                val notifications = pgConn.notifications
                if (notifications != null) {
                    for (notification in notifications) {
                        if(notification.name == "update_spec") {
                            dynamicHandlerServices.updateDynamicRoutes()
                        }
                    }
                }
                Thread.sleep(500)
            }
        }
    }

}