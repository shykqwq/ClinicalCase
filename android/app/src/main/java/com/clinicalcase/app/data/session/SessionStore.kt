package com.clinicalcase.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val displayNameKey = stringPreferencesKey("display_name")
    private val roleKey = stringPreferencesKey("role")

    val sessionFlow: Flow<Session?> = context.sessionDataStore.data.map { preferences ->
        val accessToken = preferences[accessTokenKey]
        if (accessToken.isNullOrBlank()) {
            null
        } else {
            Session(
                accessToken = accessToken,
                refreshToken = preferences[refreshTokenKey].orEmpty(),
                displayName = preferences[displayNameKey].orEmpty(),
                role = preferences[roleKey].orEmpty(),
            )
        }
    }

    suspend fun save(session: Session) {
        context.sessionDataStore.edit { preferences ->
            preferences[accessTokenKey] = session.accessToken
            preferences[refreshTokenKey] = session.refreshToken
            preferences[displayNameKey] = session.displayName
            preferences[roleKey] = session.role
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val displayName: String,
    val role: String,
)

