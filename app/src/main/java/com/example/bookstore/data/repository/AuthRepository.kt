package com.example.bookstore.data.repository

import com.example.bookstore.data.dao.UserDao
import com.example.bookstore.data.entity.User
import com.example.bookstore.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import com.example.bookstore.data.session.SessionManager
import android.database.sqlite.SQLiteConstraintException

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
        val insertedId = try {
            userDao.insertUser(user)
        } catch (e: SQLiteConstraintException) {
            return null
        }

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

    suspend fun updateProfile(
        userId: Long,
        fullName: String,
        email: String,
        phoneNumber: String,
        newPassword: String? = null
    ): User? {
        val currentUser = userDao.getUserById(userId) ?: return null
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)

        if ((normalizedEmail != currentUser.email) && userDao.isEmailExists(normalizedEmail)) {
            return null
        }

        var hash = currentUser.passwordHash
        var salt = currentUser.passwordSalt

        if (!newPassword.isNullOrBlank()) {
            salt = PasswordHasher.generateSalt()
            hash = withContext(Dispatchers.Default) {
                PasswordHasher.hashPassword(newPassword, salt)
            }
        }

        val updatedUser = currentUser.copy(
            fullName = fullName.trim(),
            email = normalizedEmail,
            phoneNumber = phoneNumber.trim(),
            passwordHash = hash,
            passwordSalt = salt
        )

        userDao.updateUser(updatedUser)
        return updatedUser
    }
}