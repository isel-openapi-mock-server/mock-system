package isel.openapi.mock.repository.jdbi

import isel.openapi.mock.domain.dynamic.ResponseInfo
import isel.openapi.mock.domain.dynamic.ScenarioInfo
import isel.openapi.mock.domain.dynamic.ScenarioNameAndId
import isel.openapi.mock.domain.dynamic.SpecAndScenario
import isel.openapi.mock.domain.openAPI.HttpMethod
import isel.openapi.mock.domain.openAPI.OpenAPIDetails
import isel.openapi.mock.domain.openAPI.PathOperations
import isel.openapi.mock.domain.openAPI.SpecInfo
import isel.openapi.mock.domain.openAPI.StatusCode
import isel.openapi.mock.repository.OpenAPIRepository
import isel.openapi.mock.services.ResponseConfig
import isel.openapi.mock.services.Scenario
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiOpenAPIRepository(
    private val handle: Handle,
) : OpenAPIRepository {

    override fun uploadOpenAPI(): List<SpecAndScenario> {

        val toReturn = mutableListOf<SpecAndScenario>()

        val transactionTokens = handle.createQuery(
            """
            SELECT uuid FROM transactions
            """
        )
            .mapTo<String>()
            .list()

        val specsIds = handle.createQuery(
            """
            SELECT id FROM specs WHERE transaction_token IN (<tokens>)
            """
        )
            .bindList("tokens", transactionTokens)
            .mapTo<Int>()
            .list()

        if (specsIds.isEmpty()) return emptyList()

        for (i in specsIds) {

            val temp = handle.createQuery(
                """
            SELECT id, name, description, transaction_token FROM specs WHERE id = :id
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

            val host = handle.createQuery(
                """
                SELECT host FROM transactions WHERE uuid = :uuid
                """
            )
                .bind("uuid", temp.transactionToken)
                .mapTo<String>()
                .first()

            val scenarios = handle.createQuery(
                """
                SELECT name as scenarioName, spec_id, method, path FROM scenarios WHERE spec_id = :id
                """
            )
                .bind("id", i)
                .mapTo<ScenarioNameAndId>()
                .list()

            val allScenarios = mutableListOf<ScenarioInfo>()

            for (scenario in scenarios) {

                val responses = handle.createQuery(
                    """
                    SELECT status_code, content_type, headers, body FROM SCENARIO_RESPONSES
                    WHERE spec_id = :spec_id AND scenario_name = :scenario_name
                    order by index
                    """
                )
                    .bind("spec_id", scenario.specId)
                    .bind("scenario_name", scenario.scenarioName)
                    .mapTo<ResponseInfo>()
                    .list()

                allScenarios.add(
                    ScenarioInfo(
                        name = scenario.scenarioName,
                        method = scenario.method,
                        path = scenario.path,
                        responses = responses
                    )
                )

            }

            toReturn.add(
                SpecAndScenario(
                    spec = SpecInfo(
                        name = temp.name,
                        description = temp.description,
                        paths = pathsInfo
                    ),
                    scenarios = allScenarios,
                    host = host
                )
            )

        }

        return toReturn

    }

}