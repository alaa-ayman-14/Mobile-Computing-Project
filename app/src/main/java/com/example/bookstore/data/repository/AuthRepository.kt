package com.example.bookstore.data.repository

import com.example.bookstore.data.dao.UserDao
import com.example.bookstore.data.entity.User
import com.example.bookstore.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import com.example.bookstore.data.session.SessionManager

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
){

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String
    ): User? {

        val normalizedEmail = email.trim().lowercase(Locale.ROOT)

        // Check if email already exists
        if (userDao.isEmailExists(normalizedEmail)) {
            return null
        }

        // Generate a unique salt
        val salt = PasswordHasher.generateSalt()

        // Hash the password in a background dispatcher
        val hash = withContext(Dispatchers.Default) {
            PasswordHasher.hashPassword(password, salt)
        }

        // Create new user
        val user = User(
            fullName = fullName.trim(),
            email = normalizedEmail,
            passwordHash = hash,
            passwordSalt = salt,
            phoneNumber = phoneNumber.trim()
        )

        // Save user in database
        val insertedId = userDao.insertUser(user)

        return user.copy(id = insertedId)
    }


    suspend fun login(
        email: String,
        password: String
    ): User? {

        val normalizedEmail = email.trim().lowercase(Locale.ROOT)

        val user = userDao.getUserByEmail(normalizedEmail)
            ?: return null

        val isPasswordCorrect = withContext(Dispatchers.Default) {
            PasswordHasher.verifyPassword(
                enteredPassword = password,
                storedSalt = user.passwordSalt,
                storedHash = user.passwordHash
            )
        }

        if (!isPasswordCorrect) {
            return null
        }

        // Save the logged-in user's session
        sessionManager.saveUserSession(user.id)

        return user
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }
}