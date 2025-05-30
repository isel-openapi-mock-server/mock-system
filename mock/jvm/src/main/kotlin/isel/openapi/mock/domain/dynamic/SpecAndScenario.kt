package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.domain.openAPI.SpecInfo

class SpecAndScenario(
    val spec: SpecInfo,
    val scenarios: List<ScenarioInfo>,
    val host: String
)