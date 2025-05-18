package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.http.DynamicHandler
import isel.openapi.mock.services.Scenario

class HandlerAndUUID(
    val dynamicHandler: DynamicHandler,
    val resourceUrl: String,
    //val scenarios: List<Scenario>
    //val isRootUpToDate: Boolean,
)