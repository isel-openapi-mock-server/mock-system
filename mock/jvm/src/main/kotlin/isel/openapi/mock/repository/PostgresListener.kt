package isel.openapi.mock.repository

import jakarta.annotation.PostConstruct
import org.postgresql.PGConnection
import org.springframework.stereotype.Component
import javax.sql.DataSource
import kotlin.concurrent.thread

@Component
class PostgresListener(
    private val dataSource: DataSource,
) {

    @PostConstruct
    fun startListening() {
        val conn = dataSource.connection
        val pgConn = conn.unwrap(PGConnection::class.java)
        conn.createStatement().use {
            it.execute("LISTEN canal_exemplo")
        }
        var count = 0
        thread {
            while (true) {
                // Aguardar novas notificações
                val notifications = pgConn.notifications
                if (notifications != null) {
                    for (notification in notifications) {
                        println("Notificação recebida: canal=${notification.name}, payload=${notification.parameter}")
                        count++
                        // Aqui você pode emitir eventos, chamar serviços, etc.
                    }
                }
                Thread.sleep(500) // polling básico
            }
        }
    }

}