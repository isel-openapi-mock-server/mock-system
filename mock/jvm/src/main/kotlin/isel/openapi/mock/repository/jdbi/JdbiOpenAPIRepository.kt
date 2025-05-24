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

    override fun uploadOpenAPI(): List<Pair<String, SpecInfo>> {

        val toReturn = mutableListOf<Pair<String, SpecInfo>>()

        val specsIds = handle.createQuery(
            """
            SELECT id FROM specs
            """
        )
            .mapTo<Int>()
            .list()

        if (specsIds.isEmpty()) return emptyList()

        for (i in specsIds) {

            val temp = handle.createQuery(
                """
            SELECT id, name, description, host FROM specs WHERE id = :id
            """
            )
                .bind("id", i)
                .mapTo<OpenAPIDetails>()
                .first()

            val pathsIds = handle.createQuery(
                """
            SELECT id FROM paths WHERE spec_id = :id
            """
            )
                .bind("id", i)
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

            toReturn.add(
                Pair(
                    temp.host,
                    SpecInfo(
                        name = temp.name,
                        description = temp.description,
                        paths = pathsInfo
                    )
                )
            )

        }

        return toReturn

    }

}