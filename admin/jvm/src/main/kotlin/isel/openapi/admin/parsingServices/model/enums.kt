package isel.openapi.admin.parsingServices.model

enum class HttpMethod{
    GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD, TRACE
}

enum class Location{
    QUERY, HEADER, PATH, COOKIE, UNKNOWN
}

enum class ParameterStyle{
    FORM, SPACEDELIMITED, PIPEDELIMITED, DEEPOBJECT, SIMPLE, MATRIX, LABEL
}
