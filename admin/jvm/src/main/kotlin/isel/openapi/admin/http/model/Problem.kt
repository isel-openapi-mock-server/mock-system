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
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-open-api-spec.txt",
                ),
            )

        val pathOperationDoesNotExist =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/path-operation-does-not-exist.txt",
                ),
            )

        val invalidResponseContent =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-response-content.txt",
                ),
            )

        val invalidTransaction =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-transaction.txt",
                ),
            )

        val hostDoesNotExist =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/host-does-not-exist.txt",
                ),
            )

        val transactionOrHostNotProvided =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/transaction-or-host-not-provided.txt",
                ),
            )

        val requestNotFound =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/request-not-found.txt",
                ),
            )

        val requestCredentialIsRequired =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/request-credential-is-required.txt",
                ),
            )

        val invalidDateRange =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-date-range.txt",
                ),
            )

        val invalidMethod =
            Problem(
                URI(
                    "https://github.com/isel-openapi-mock-server/mock-system/tree/main/docs/problems/invalid-method.txt",
                ),
            )
    }
}