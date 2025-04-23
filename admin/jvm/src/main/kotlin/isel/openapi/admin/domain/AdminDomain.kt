package isel.openapi.admin.domain

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class AdminDomain {

    fun generateHost(): String =
        ByteArray(256 / 8).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

}