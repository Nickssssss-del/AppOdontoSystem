package com.odontosystem.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.odontosystem.app.data.model.UserRole

class SessionManager(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
        // RF01: Guardar también timestamp para simular expiración JWT
        prefs.edit().putLong(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    fun saveRefreshToken(refreshToken: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    fun isTokenExpired(): Boolean {
        val timestamp = prefs.getLong(KEY_TOKEN_TIMESTAMP, 0L)
        if (timestamp == 0L) return true
        // Simular expiración de 2 horas para el token JWT
        val twoHoursMillis = 2 * 60 * 60 * 1000
        return System.currentTimeMillis() - timestamp > twoHoursMillis
    }

    fun fetchAuthToken(): String? {
        if (isTokenExpired()) {
            clearSession()
            return null
        }
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun fetchRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun fetchUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun fetchUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun saveUserRole(role: UserRole) {
        prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
    }

    fun fetchUserRole(): UserRole {
        val roleStr = prefs.getString(KEY_USER_ROLE, UserRole.PATIENT.name)
        return try {
            UserRole.valueOf(roleStr ?: UserRole.PATIENT.name)
        } catch (e: Exception) {
            UserRole.PATIENT
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return !fetchAuthToken().isNullOrEmpty()
    }

    companion object {
        private const val PREF_NAME = "encrypted_session_prefs"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_TOKEN_TIMESTAMP = "token_timestamp"
    }
}
