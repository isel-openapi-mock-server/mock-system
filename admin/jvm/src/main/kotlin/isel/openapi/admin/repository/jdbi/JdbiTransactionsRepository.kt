package isel.openapi.admin.repository.jdbi

import isel.openapi.admin.repository.TransactionsRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiTransactionsRepository(
    private val handle: Handle,
) : TransactionsRepository {

    override fun isTransactionActive(uuid: String): Boolean {
        val id = handle.createQuery(
            """
            SELECT uuid from open_transactions
            """
        )
            .mapTo<String>()
            .one()
        return id == uuid
    }

    override fun commitTransaction(uuid: String, host: String) {
        handle.createUpdate(
            """
            delete from open_transactions
            where uuid = :uuid
            """
        )
            .bind("uuid", uuid)
            .execute()

        val isHostExists = handle.createQuery(
            """
            select uuid from transactions
            where host = :host
            """
        )
            .bind("host", host)
            .mapTo<Boolean>()
            .one()

        if(!isHostExists) {
            handle.createUpdate(
                """
                insert into transactions (uuid, host)
                values (:uuid, :host)
                """
            )
                .bind("uuid", uuid)
                .bind("host", host)
                .execute()
        } else {
            handle.createUpdate(
                """
                update transactions
                set uuid = :uuid
                where host = :host
                """
            )
                .bind("uuid", uuid)
                .bind("host", host)
                .execute()
        }
    }

    override fun isHostExists(host: String): Boolean {
        return handle.createQuery(
            """
            SELECT uuid FROM transactions
            WHERE host = :host
            """
        )
            .bind("host", host)
            .mapTo<String>()
            .firstOrNull() != null
    }

}