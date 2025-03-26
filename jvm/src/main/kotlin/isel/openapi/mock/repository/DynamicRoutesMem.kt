package isel.openapi.mock.repository

import isel.openapi.mock.parsingServices.model.PathOperation
import isel.openapi.mock.parsingServices.model.ApiParameter
import isel.openapi.mock.parsingServices.model.ApiRequestBody
import isel.openapi.mock.parsingServices.model.HttpMethod
import java.util.concurrent.ConcurrentHashMap


class DynamicRoutesMem {

    private val dynamicGetRoutes = ConcurrentHashMap<String, PathOperation>()
    private val dynamicPostRoutes = ConcurrentHashMap<String, PathOperation>()
    private val dynamicPutRoutes = ConcurrentHashMap<String, PathOperation>()
    private val dynamicDeleteRoutes = ConcurrentHashMap<String, PathOperation>()

    fun addDynamicRoute(path: String, mappingInfo: PathOperation) {
        when (mappingInfo.method) {
            HttpMethod.GET -> dynamicGetRoutes[path] = mappingInfo
            HttpMethod.POST -> dynamicPostRoutes[path] = mappingInfo
            HttpMethod.PUT -> dynamicPutRoutes[path] = mappingInfo
            HttpMethod.DELETE -> dynamicDeleteRoutes[path] = mappingInfo
            else -> throw IllegalArgumentException("Unsupported method: ${mappingInfo.method}")
        }
    }

    fun removeDynamicRoute(path: String): PathOperation? {
        TODO()
    }

    fun getRoute(path: String, method: PathOperation): PathOperation? {
        return when (method.method) {
            HttpMethod.GET -> dynamicGetRoutes[path]
            HttpMethod.POST -> dynamicPostRoutes[path]
            HttpMethod.PUT -> dynamicPutRoutes[path]
            HttpMethod.DELETE -> dynamicDeleteRoutes[path]
            else -> throw IllegalArgumentException("Unsupported method: ${method.method}")
        }
    }

    fun getParams(path: String, method: PathOperation): List<ApiParameter>? {
        val info = getRoute(path, method)
        return info?.parameters
    }

    fun getBody(path: String, method: PathOperation): ApiRequestBody? {
        val info = getRoute(path, method)
        return info?.requestBody
    }

}