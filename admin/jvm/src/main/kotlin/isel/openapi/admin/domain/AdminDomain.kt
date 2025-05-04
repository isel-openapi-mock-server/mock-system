package isel.openapi.admin.domain

import isel.openapi.admin.parsingServices.model.Response
import isel.openapi.admin.parsingServices.model.StatusCode
import isel.openapi.admin.utils.Either
import isel.openapi.admin.utils.failure
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

sealed interface VerifyResponseError {
    data object WrongStatusCode : VerifyResponseError
}

typealias VerifyResponseResult = Either<VerifyResponseError, Boolean>

@Component
class AdminDomain {

    fun generateHost(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val length = 16
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    fun verifyResponse(responseSpec: Response, statusCode: StatusCode, headers: Map<String, String>?, body: ByteArray?):  VerifyResponseResult{
        if (responseSpec.statusCode != statusCode) {
            failure(VerifyResponseError.WrongStatusCode)
        }
        TODO()
    }

    private fun verifyHeaders() {
        TODO()
    }

    private fun verifyBody() {
        TODO()
    }

}