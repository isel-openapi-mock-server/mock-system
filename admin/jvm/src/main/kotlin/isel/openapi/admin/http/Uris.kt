package isel.openapi.admin.http

object Uris {

    const val API = "/admin"

    object Admin {
        const val API_SPEC = "$API/openapi"
        const val RESPONSES = "$API/response"
        const val COMMIT = "$API/commit"
    }

    object Requests {
        const val REQUESTS = "$API/requests"
        const val REQUESTS_SEARCH = "$API/requests/search"
    }

}