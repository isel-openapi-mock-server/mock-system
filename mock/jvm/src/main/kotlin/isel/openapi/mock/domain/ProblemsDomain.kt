package isel.openapi.mock.domain

import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import java.security.SecureRandom
import java.util.*

@Component
class ProblemsDomain {
    fun generateUuidValue(): String =
        ByteArray(256 / 8).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun canBeUuid(uuid: String): Boolean =
        try {
            Base64.getUrlDecoder()
                .decode(uuid).size == 256 / 8
        } catch (ex: IllegalArgumentException) {
            false
        }
}