package com.example.bookstore.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashPassword(password: String, saltString: String): String {

        val salt = Base64.decode(saltString, Base64.NO_WRAP)

        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERATIONS,
            KEY_LENGTH
        )

        return try {
            val factory = SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
            )

            val hash = factory.generateSecret(spec).encoded

            Base64.encodeToString(hash, Base64.NO_WRAP)

        } finally {
            spec.clearPassword()
        }
    }

    fun verifyPassword(enteredPassword: String, storedSalt: String, storedHash: String): Boolean {

        val enteredHash = hashPassword(enteredPassword, storedSalt)

        val enteredBytes = Base64.decode(enteredHash, Base64.NO_WRAP)

        val storedBytes = Base64.decode(storedHash, Base64.NO_WRAP)

        return java.security.MessageDigest.isEqual(enteredBytes, storedBytes)
    }
}