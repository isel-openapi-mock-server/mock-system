package isel.openapi.admin.repository.jdbi

import isel.openapi.admin.parsing.model.HttpMethod
import isel.openapi.admin.repository.TransactionsRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.postgresql.util.PGobject

class JdbiTransactionsRepository(
    private val handle: Handle,
) : TransactionsRepository {

    override fun isTransactionActive(uuid: String): Boolean {
        return handle.createQuery(
            """
            SELECT uuid from open_transactions
            where uuid = :uuid and isAlive = true
            """
        )
            .bind("uuid", uuid)
            .mapTo<String>()
            .firstOrNull() != null
    }

    override fun commitTransaction(uuid: String, host: String) {
        handle.createUpdate(
            """
            update open_transactions
            set isAlive = false, host = :host
            where uuid = :uuid
            """
        )
            .bind("uuid", uuid)
            .bind("host", host)
            .execute()

        val isHostExists = handle.createQuery(
            """
            select uuid from transactions
            where host = :host
            """
        )
            .bind("host", host)
            .mapTo<String>()
            .firstOrNull() != null

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

    override fun getTransactionByHost(host: String): String? {
        return handle.createQuery(
            """
            SELECT uuid FROM transactions
            WHERE host = :host
            """
        )
            .bind("host", host)
            .mapTo<String>()
            .firstOrNull()
    }

    override fun addNewTransaction(uuid: String, specId: Int, host: String?) {
        handle.createUpdate(
            """
            INSERT INTO open_transactions (uuid, host, spec_id)
            VALUES (:uuid, :host, :specId)
            """
        )
            .bind("uuid", uuid)
            .bind("host", host)
            .bind("specId", specId)
            .execute()
    }

    override fun copySpecToTransaction(transactionToken: String, specId: Int): Int {
        val newSpecId = handle.createUpdate(
            """
            INSERT INTO specs (name, description, transaction_token)
            SELECT name, description, :transactionToken
            FROM specs
            WHERE id = :specId
            """
        )
            .bind("specId", specId)
            .bind("transactionToken", transactionToken)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .first()

        val pathsIds = handle.createQuery(
            """
            SELECT id FROM paths WHERE spec_id = :specId
            """
        )
            .bind("specId", specId)
            .mapTo<Int>()
            .list()

        for (pathId in pathsIds) {
            handle.createUpdate(
                """
                INSERT INTO paths (full_path, operations, spec_id)
                SELECT full_path, operations, :newSpecId
                FROM paths
                WHERE id = :pathId
                """
            )
                .bind("pathId", pathId)
                .bind("newSpecId", newSpecId)
                .execute()
        }

        println(newSpecId)

        return newSpecId

    }

    override fun getScenarioNameByTransaction(transactionToken: String): String? {
        return handle.createQuery(
            """
            SELECT name FROM scenarios
            WHERE transaction_token = :transactionToken
            """
        )
            .bind("transactionToken", transactionToken)
            .mapTo<String>()
            .firstOrNull()
    }

    override fun deleteScenario(transactionToken: String, scenarioName: String): Boolean {

        val rowsAffected = handle.createUpdate(
            """
            DELETE FROM scenarios
            WHERE transaction_token = :transactionToken AND name = :scenarioName
            """
        )
            .bind("transactionToken", transactionToken)
            .bind("scenarioName", scenarioName)
            .execute()

        return rowsAffected > 0
    }

    override fun addScenario(transactionToken: String, scenarioName: String, method: String, path: String, specId: Int) {

        println(specId)

        handle.createUpdate(
            """
            INSERT INTO scenarios (transaction_token, name, spec_id, method, path)
            VALUES (:transactionToken, :scenarioName, :specId, :method, :path)
            """
        )
            .bind("transactionToken", transactionToken)
            .bind("scenarioName", scenarioName)
            .bind("specId", specId)
            .bind("method", method.uppercase())
            .bind("path", path)
            .execute()
    }

    override fun addScenarioResponse(
        transactionToken: String,
        scenarioName: String,
        index: Int,
        statusCode: String,
        body: ByteArray?,
        headers: String?,
        contentType: String?,
        specId: Int
    ): Boolean {

        val rowsAffected = handle.createUpdate(
            """
            INSERT INTO scenario_responses (scenario_name, index, status_code, body, headers, content_type, spec_id)
            VALUES (:scenarioName, :index, :statusCode, :body, :headers, :contentType, :specId)
            """
        )
            .bind("transactionToken", transactionToken)
            .bind("scenarioName", scenarioName)
            .bind("index", index)
            .bind("statusCode", statusCode)
            .bind("body", body)
            .bind("headers", if(headers != null)jsonb(headers) else null)
            .bind("contentType", contentType)
            .bind("specId", specId)
            .execute()

        return rowsAffected > 0
    }

    override fun getSpecIdByTransaction(transactionToken: String): Int? {
        return handle.createQuery(
            """
            SELECT spec_id FROM open_transactions
            WHERE uuid = :transactionToken
            """
        )
            .bind("transactionToken", transactionToken)
            .mapTo<Int>()
            .firstOrNull()
    }

    override fun getHostByTransactionToken(transactionToken: String): String? {
        return handle.createQuery(
            """
            SELECT host FROM open_transactions
            WHERE uuid = :transactionToken
            """
        )
            .bind("transactionToken", transactionToken)
            .mapTo<String>()
            .firstOrNull()
    }

    private fun jsonb(value: String): PGobject {
        return PGobject().apply {
            type = "jsonb"
            setValue(value)
        }
    }

}