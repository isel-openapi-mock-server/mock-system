package isel.openapi.mock.repository

import isel.openapi.mock.domain.openAPI.SpecInfo

interface OpenAPIRepository {

    fun uploadOpenAPI(): List<Pair<String, SpecInfo>>

}