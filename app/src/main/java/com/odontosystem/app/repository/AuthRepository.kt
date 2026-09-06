package com.odontosystem.app.repository

import com.odontosystem.app.data.local.SessionManager
import com.odontosystem.app.data.model.AuthRequest
import com.odontosystem.app.data.model.AuthResponse
import com.odontosystem.app.data.model.RegisterRequest
import com.odontosystem.app.data.model.UserRole
import com.odontosystem.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun login(email: String, pass: String, role: UserRole): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(AuthRequest(email, pass, role))
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!
                    sessionManager.saveAuthToken(authBody.token)
                    sessionManager.saveRefreshToken(authBody.refreshToken)
                    sessionManager.saveUserEmail(authBody.user.email)
                    sessionManager.saveUserName(authBody.user.name)
                    sessionManager.saveUserRole(role)
                    Result.success(authBody)
                } else {
                    // Propagar el error real del backend (401, 500, etc.)
                    Result.failure(Exception(errorMessage(response)))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(request)
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!
                    sessionManager.saveAuthToken(authBody.token)
                    sessionManager.saveRefreshToken(authBody.refreshToken)
                    sessionManager.saveUserEmail(authBody.user.email)
                    sessionManager.saveUserName(authBody.user.name)
                    sessionManager.saveUserRole(request.role)
                    Result.success(authBody)
                } else {
                    Result.failure(Exception(errorMessage(response)))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Extrae el mensaje de error estándar del backend ({ timestamp, status, message })
     * para mostrarlo tal cual al usuario en lugar de un error genérico.
     */
    private fun errorMessage(response: Response<*>): String {
        val code = response.code()
        return when (code) {
            401 -> "Credenciales inválidas (correo o contraseña incorrectos)."
            404 -> "El recurso solicitado no existe (${code})."
            409 -> "Conflicto: ya existe un registro con esos datos."
            else -> response.errorBody()?.string()?.let { body ->
                extractMessage(body) ?: "Error del servidor (${code})."
            } ?: "Error del servidor (${code})."
        }
    }

    private fun extractMessage(body: String): String? {
        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(body)
            ?.groupValues
            ?.getOrNull(1)
    }

    fun loginAsDemo(role: UserRole, customEmail: String? = null): Boolean {
        sessionManager.saveAuthToken("demo_jwt_token")
        sessionManager.saveUserRole(role)
        if (role == UserRole.PATIENT) {
            sessionManager.saveUserEmail(customEmail ?: "paciente@odontosystem.com")
            sessionManager.saveUserName("Nicole De La Cruz")
        } else {
            sessionManager.saveUserEmail(customEmail ?: "dr.ramos@odontosystem.com")
            sessionManager.saveUserName("Dr. Roberto Ramos")
        }
        return true
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
    fun getUserRole(): UserRole = sessionManager.fetchUserRole()
}