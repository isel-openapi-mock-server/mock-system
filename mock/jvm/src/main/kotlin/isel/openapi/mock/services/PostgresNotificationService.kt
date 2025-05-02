import jakarta.annotation.PostConstruct
import org.jdbi.v3.core.Jdbi
import org.postgresql.PGConnection
import org.springframework.stereotype.Service

@Service
class PostgresNotificationService(
    private val jdbi: Jdbi,
) {

    @PostConstruct
    fun startListening() {
         jdbi.open().use { handler ->
             val connection = handler.connection

             connection.createStatement().use { statement ->
                 statement.execute("LISTEN openApi_notifications")
             }

             Thread {
                    while (true) {
                        val pgConnection = connection.unwrap(PGConnection::class.java)
                        pgConnection.getNotifications(0)?.forEach { notification ->
                            println("Received notification: ${notification.name} - ${notification.parameter}")
                        }
                        Thread.sleep(1000)
                    }
                }.start()

         }

    }

}