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
            it.execute("LISTEN new_spec")
        }
        thread {
            while (true) {
                val notifications = pgConn.notifications
                if (notifications != null) {
                    for (notification in notifications) {
                        if(notification.name == "new_spec") {
                            // Aqui você pode processar a notificação recebida
                            // Por exemplo, você pode chamar um método para lidar com a notificação
                            // ou simplesmente imprimir uma mensagem no console
                            println("Notificação recebida: ${notification.parameter}")
                        }
                    }
                }
                Thread.sleep(500) // polling básico
            }
        }
    }

}