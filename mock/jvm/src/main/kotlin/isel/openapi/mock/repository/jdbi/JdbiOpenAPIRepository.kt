package isel.openapi.mock.repository.jdbi

import isel.openapi.mock.domain.openAPI.OpenAPIDetails
import isel.openapi.mock.domain.openAPI.PathOperations
import isel.openapi.mock.domain.openAPI.SpecInfo
import isel.openapi.mock.repository.OpenAPIRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiOpenAPIRepository(
    private val handle: Handle,
) : OpenAPIRepository {

    override fun uploadOpenAPI(
        host: String,
    ): SpecInfo? {
        val temp = handle.createQuery(
            """
            SELECT id, name, description FROM specs WHERE host = :host
            """
        )
            .bind("host", host)
            .mapTo<OpenAPIDetails>()
            .firstOrNull()

        if(temp == null) return null

        val pathsIds = handle.createQuery(
            """
            SELECT id FROM paths WHERE spec_id = :id
            """
        )
            .bind("id", temp.id)
            .mapTo<Int>()
            .list()

        val pathsInfo = mutableListOf<PathOperations>()

        for(pathId in pathsIds) {
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

}