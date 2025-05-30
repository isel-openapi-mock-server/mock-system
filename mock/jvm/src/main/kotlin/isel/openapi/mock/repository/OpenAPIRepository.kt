package isel.openapi.mock.repository

import isel.openapi.mock.domain.dynamic.SpecAndScenario

interface OpenAPIRepository {

    fun uploadOpenAPI(): List<SpecAndScenario>

}