package isel.openapi.mock.repository

import isel.openapi.mock.utils.ApiMethod
import isel.openapi.mock.utils.ApiParameter
import isel.openapi.mock.utils.ApiRequestBody
import java.util.concurrent.ConcurrentHashMap


class DynamicRoutesMem {

    private val dynamicGetRoutes = ConcurrentHashMap<String, ApiMethod>()
    private val dynamicPostRoutes = ConcurrentHashMap<String, ApiMethod>()
    private val dynamicPutRoutes = ConcurrentHashMap<String, ApiMethod>()
    private val dynamicDeleteRoutes = ConcurrentHashMap<String, ApiMethod>()

    fun addDynamicRoute(path: String, mappingInfo: ApiMethod) {
        when (mappingInfo.method) {
            "GET" -> dynamicGetRoutes[path] = mappingInfo
            "POST" -> dynamicPostRoutes[path] = mappingInfo
            "PUT" -> dynamicPutRoutes[path] = mappingInfo
            "DELETE" -> dynamicDeleteRoutes[path] = mappingInfo
            else -> throw IllegalArgumentException("Unsupported method: ${mappingInfo.method}")
        }
    }

    fun removeDynamicRoute(path: String): ApiMethod? {
        TODO()
    }

    fun getRoute(path: String, method: ApiMethod): ApiMethod? {
        return when (method.method) {
            "GET" -> dynamicGetRoutes[path]
            "POST" -> dynamicPostRoutes[path]
            "PUT" -> dynamicPutRoutes[path]
            "DELETE" -> dynamicDeleteRoutes[path]
            else -> throw IllegalArgumentException("Unsupported method: ${method.method}")
        }
    }

    fun getParams(path: String, method: ApiMethod): List<ApiParameter>? {
        val info = getRoute(path, method)
        return info?.parameters
    }

    fun getBody(path: String, method: ApiMethod): ApiRequestBody? {
        val info = getRoute(path, method)
        return info?.requestBody
    }

}