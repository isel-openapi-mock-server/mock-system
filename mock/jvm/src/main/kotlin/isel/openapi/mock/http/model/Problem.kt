package isel.openapi.mock.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI,
) {
    val type = typeUri.toString()

    companion object {
        const val MEDIA_TYPE = "application/problem+json"

        fun response(
            status: Int,
            problem: Problem,
            exchangeKey: String? = null
        ): ResponseEntity<*> {
            val rsp = ResponseEntity
                .status(status)
                .header("Content-Type", MEDIA_TYPE)
            if(exchangeKey != null) {
                rsp.header("Exchange-Key", exchangeKey)
            }
            return rsp.body(problem)
        }

        val hostDoesNotExist =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/host-does-not-exist",
                ),
            )

        val handlerNotFound =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/handler-not-found",
                ),
            )

        val scenarioNotFound =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/scenario-not-found",
                ),
            )

        val noResponseForThisRequestInScenario =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/no-response-for-this-request-in-scenario",
                ),
            )

        val noResponseForThisRequest =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/no-response-for-this-request",
                ),
            )

        val badRequest =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/bad-request",
                ),
            )

    }
}