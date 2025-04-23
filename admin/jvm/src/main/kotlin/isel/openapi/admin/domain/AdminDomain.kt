package isel.openapi.admin.domain

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class AdminDomain {

    fun generateHost(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val length = 16
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

}