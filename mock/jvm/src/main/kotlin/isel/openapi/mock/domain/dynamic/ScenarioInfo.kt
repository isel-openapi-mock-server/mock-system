package isel.openapi.mock.domain.dynamic

class ScenarioInfo(
    val name: String,
    val method: String,
    val path: String,
    val responses: List<ResponseInfo>
)