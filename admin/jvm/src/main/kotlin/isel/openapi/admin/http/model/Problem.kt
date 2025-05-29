package isel.openapi.admin.http.model

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
        ) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        val invalidOpenAPISpec =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/invalid-open-api-spec",
                ),
            )

        val pathOperationDoesNotExist =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/path-operation-does-not-exist",
                ),
            )

        val invalidResponseContent =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/invalid-response-content",
                ),
            )

        val invalidTransaction =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/invalid-transaction",
                ),
            )

        val hostDoesNotExist =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/host-does-not-exist",
                ),
            )

        val transactionOrHostNotProvided =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/transaction-or-host-not-provided",
                ),
            )

        val requestNotFound =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/request-not-found",
                ),
            )

        val requestCredentialIsRequired =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/request-credential-is-required",
                ),
            )

        val invalidDateRange =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/invalid-date-range",
                ),
            )

        val invalidMethod =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/private-spring/tree/main/docs/problems/invalid-method",
                ),
            )
    }
}