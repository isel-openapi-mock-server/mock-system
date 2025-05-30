package isel.openapi.admin.repository.jdbi

import isel.openapi.admin.domain.admin.OpenAPIDetails
import isel.openapi.admin.domain.admin.PathOperations
import isel.openapi.admin.domain.admin.SpecInfo
import isel.openapi.admin.repository.AdminRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.postgresql.util.PGobject

class JdbiAdminRepository(
    private val handle: Handle,
) : AdminRepository {

    override fun addAPISpec(name: String, description: String?, transactionToken: String): Int {
        return handle.createUpdate(
            """
            INSERT INTO specs (name, description, transaction_token) VALUES (:name, :description, :transaction)
            """
        )
            .bind("name", name)
            .bind("description", description)
            .bind("transaction", transactionToken)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun addPath(specId: Int, path: String, operations: String) {
        handle.createUpdate(
            """
            INSERT INTO paths (full_path, operations, spec_id) VALUES (:path, :operations, :id)
            """
        )
            .bind("path", path)
            .bind("operations", jsonb(operations))
            .bind("id", specId)
            .execute()
    }

    override fun getSpecId(host: String): Int? {

        val transactionToken = handle.createQuery(
            """
            SELECT uuid FROM open_transactions WHERE host = :host
            """
        )
            .bind("host", host)
            .mapTo<String>()
            .firstOrNull()

        if (transactionToken == null) return null

        return handle.createQuery(
            """
            SELECT id FROM specs WHERE transaction = :transaction
            """
        )
            .bind("transaction", transactionToken)
            .mapTo<Int>()
            .firstOrNull()
    }

    override fun updateAPISpec(id: Int, name: String, description: String?) {
        handle.createUpdate(
            """
            UPDATE specs SET name = :name, description = :description WHERE id = :id
            """
        )
            .bind("name", name)
            .bind("description", description)
            .bind("id", id)
            .execute()

        handle.createUpdate(
            """
            DELETE FROM paths WHERE spec_id = :id
            """
        )
            .bind("id", id)
            .execute()
    }

    override fun getApiSpecByTransactionToken(transactionToken: String): SpecInfo? {

        val specId = handle.createQuery(
            """
            SELECT id FROM specs
            where transaction = :transactionToken
            """
        )
            .mapTo<Int>()
            .firstOrNull() ?: return null


        val temp = handle.createQuery(
            """
        SELECT id, name, description FROM specs WHERE id = :id
        """
        )
            .bind("id", specId)
            .mapTo<OpenAPIDetails>()
            .first()

        val pathsIds = handle.createQuery(
            """
        SELECT id FROM paths WHERE spec_id = :id
        """
        )
            .bind("id", specId)
            .mapTo<Int>()
            .list()

        val pathsInfo = mutableListOf<PathOperations>()

        for (pathId in pathsIds) {
            val operation = handle.createQuery(
                """
            SELECT full_path as path, operations FROM paths WHERE id = :pathId
            """
            )
                .bind("pathId", pathId)
                .mapTo<PathOperations>()
                .first()

            pathsInfo.add(operation)

        }

        return SpecInfo(
                    name = temp.name,
                    description = temp.description,
                    paths = pathsInfo
                )

    }

    private fun jsonb(value: String): PGobject {
        return PGobject().apply {
            type = "jsonb"
            setValue(value)
        }
    }

}