package isel.openapi.mock.repository

import org.jdbi.v3.core.Handle
import javax.sql.DataSource

class JdbiListener(
    private val dataSource: DataSource,
) {

    /*
    fun changesListeners(
        listener: String,
        onEvent: (String) -> Unit,
    ) {

        dataSource.connection.createStatement().execute(
            """
            LISTEN $listener;
            """
        )
    }

     */

}