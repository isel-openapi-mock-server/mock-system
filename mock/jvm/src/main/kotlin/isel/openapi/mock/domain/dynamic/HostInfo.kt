package isel.openapi.mock.domain.dynamic

import isel.openapi.mock.services.Scenario

class HostInfo(
    val root: RouteNode,
    //val isRootUpToDate: Boolean,
    val scenarios: List<Scenario>
)