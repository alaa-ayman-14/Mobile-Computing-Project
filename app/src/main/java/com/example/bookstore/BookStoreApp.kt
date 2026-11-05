package com.example.bookstore

import android.app.Application
import com.example.bookstore.data.database.DatabaseProvider
import com.example.bookstore.data.repository.AuthRepository
import com.example.bookstore.data.session.SessionManager

class BookStoreApp : Application() {

    val database by lazy {
        DatabaseProvider.getDatabase(this)
    }

    val sessionManager by lazy {
        SessionManager(this)
    }

    val authRepository by lazy {
        AuthRepository(
            userDao = database.userDao(),
            sessionManager = sessionManager
        )
    }
}