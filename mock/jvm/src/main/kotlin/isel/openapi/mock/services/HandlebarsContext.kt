package isel.openapi.mock.services

import com.fasterxml.jackson.databind.ObjectMapper
import isel.openapi.mock.domain.problems.ParameterInfo
import org.apache.commons.text.StringEscapeUtils
import isel.openapi.mock.domain.openAPI.Location

class HandlebarsContext {

    private val context: MutableMap<String, Any?> = mutableMapOf()

    init {
        context["randomId"] = generateId()
        context["randomString"] = generateRandomString()
    }

    private fun generateId(): Int {
        return System.currentTimeMillis().toInt()
    }

    private fun generateRandomString(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun addBody(value: String?, contentType: String): HandlebarsContext {
        if (value.isNullOrBlank()) {
            context["body"] = null
            return this
        }

        val isJson = contentType.contains("json", ignoreCase = true)
        val isUrlEncoded = contentType.contains("x-www-form-urlencoded", ignoreCase = true)

        when {
            isJson -> {
                val objectMapper = ObjectMapper()
                val jsonNode = objectMapper.readTree(value)
                val mapped = when {
                    jsonNode.isObject -> objectMapper.convertValue(jsonNode, Map::class.java) as Map<String, Any?>
                    jsonNode.isArray -> objectMapper.convertValue(jsonNode, List::class.java) as List<Any?>
                    else -> value
                }
                context["body"] = mapped
            }
            isUrlEncoded -> {
                val params = value.split("&").associate {
                    val (key, v) = it.split("=", limit = 2)
                    key to StringEscapeUtils.unescapeHtml4(v)
                }
                context["body"] = params
            }
            else -> {
                context["body"] = value
            }
        }
        return this
    }

    fun addUrl(value: String): HandlebarsContext {
        context["url"] = value
        return this
    }

    fun pathParts(value: String): HandlebarsContext {
        val parts = value.split("/").filter { it.isNotBlank() }
        context["pathParts"] = parts
        return this
    }

    fun addParams(params: List<ParameterInfo>): HandlebarsContext {
        val queryParams = mutableMapOf<String, Any?>()
        val pathParams = mutableMapOf<String, Any?>()
        val cookies = mutableMapOf<String, Any?>()
        params.forEach {
            when (it.location) {
                Location.QUERY -> {
                    queryParams[it.name] = it.content
                }
                Location.PATH -> {
                    pathParams[it.name] = it.content
                }
                Location.COOKIE -> {
                    cookies[it.name] = it.content
                }
                else -> { }
            }
        }
        context["queryParams"] = queryParams
        context["pathParams"] = pathParams
        context["cookies"] = cookies
        return this
    }

    fun addHeaders(value: Map<String, String>): HandlebarsContext {
        context["headers"] = value
        return this
    }

    fun getContext(): Map<String, Any?> {
        return context
    }

}