package isel.openapi.mock.parsingServices.model

enum class HttpMethod{
    GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD, TRACE
}

enum class Location{
    QUERY, HEADER, PATH, COOKIE, UNKNOWN;

    fun locationToString(): String {
        return when (this) {
            QUERY -> "query"
            HEADER -> "header"
            PATH -> "path"
            COOKIE -> "cookie"
            UNKNOWN -> "unknown"
        }
    }
}

enum class ParameterStyle{
    FORM, SPACEDELIMITED, PIPEDELIMITED, DEEPOBJECT, SIMPLE, MATRIX, LABEL
}
