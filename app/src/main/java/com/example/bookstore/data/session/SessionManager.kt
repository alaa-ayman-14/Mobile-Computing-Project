package com.example.bookstore.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(
    name = "user_session"
)

class SessionManager(context: Context) {

    private val dataStore = context.applicationContext.sessionDataStore

    companion object {
        private val USER_ID = longPreferencesKey("user_id")
    }

    // Get the currently logged-in user's ID
    val userId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    // Save user session after successful login
    suspend fun saveUserSession(userId: Long) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    // Clear session after logout
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID)
        }
    }
}