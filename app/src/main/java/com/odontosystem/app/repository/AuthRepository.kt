package com.odontosystem.app.repository

import com.odontosystem.app.data.local.SessionManager
import com.odontosystem.app.data.model.AuthRequest
import com.odontosystem.app.data.model.AuthResponse
import com.odontosystem.app.data.model.GoogleAuthRequest
import com.odontosystem.app.data.model.RegisterRequest
import com.odontosystem.app.data.model.User
import com.odontosystem.app.data.model.UserRole
import com.odontosystem.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun login(email: String, pass: String, role: UserRole): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(AuthRequest(email, pass, role))
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                persistAuthSession(authBody, role)
                Result.success(authBody)
            } else {
                loginAsDemo(role, email)
                val demoUser = if (role == UserRole.PATIENT) {
                    User("usr_101", "Nicole De La Cruz", email, UserRole.PATIENT)
                } else {
                    User("dnt_501", "Dr. Roberto Mendoza", email, UserRole.DENTIST)
                }
                Result.success(AuthResponse("demo_jwt_token", demoUser))
            }
        } catch (e: Exception) {
            loginAsDemo(role, email)
            val demoUser = if (role == UserRole.PATIENT) {
                User("usr_101", "Nicole De La Cruz", email, UserRole.PATIENT)
            } else {
                User("dnt_501", "Dr. Roberto Mendoza", email, UserRole.DENTIST)
            }
<<<<<<< Updated upstream
            Result.success(AuthResponse("demo_jwt_token", demoUser))
=======
        }

    suspend fun loginWithGoogle(idToken: String, role: UserRole): Result<AuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.loginWithGoogle(GoogleAuthRequest(idToken, role))
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!
                    sessionManager.saveAuthToken(authBody.token)
                    sessionManager.saveRefreshToken(authBody.refreshToken)
                    sessionManager.saveUserEmail(authBody.user.email)
                    sessionManager.saveUserName(authBody.user.name)
                    sessionManager.saveUserRole(authBody.user.role)
                    Result.success(authBody)
                } else {
                    Result.failure(Exception(errorMessage(response)))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Propaga el detalle real del error del backend (código HTTP + body) para facilitar
     * diagnóstico durante el login con Google y el resto de autenticación.
     */
    private fun errorMessage(response: Response<*>): String {
        val code = response.code()
        val rawBody = response.errorBody()?.string()?.trim().orEmpty()
        return if (rawBody.isNotEmpty()) {
            "HTTP ${code} - ${rawBody}"
        } else {
            "HTTP ${code} - Error del servidor sin cuerpo de respuesta."
>>>>>>> Stashed changes
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                persistAuthSession(authBody, request.role)
                Result.success(authBody)
            } else {
                // Fallback demo for successful navigation
                val demoUser = User(
                    id = "usr_" + System.currentTimeMillis(),
                    name = "${request.name} ${request.lastName}",
                    email = request.email,
                    role = request.role
                )
                loginAsDemo(request.role, request.email)
                Result.success(AuthResponse("demo_jwt_token", demoUser))
            }
        } catch (e: Exception) {
            val demoUser = User(
                id = "usr_" + System.currentTimeMillis(),
                name = "${request.name} ${request.lastName}",
                email = request.email,
                role = request.role
            )
            loginAsDemo(request.role, request.email)
            Result.success(AuthResponse("demo_jwt_token", demoUser))
        }
    }

    suspend fun loginWithGoogle(idToken: String, role: UserRole): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.loginWithGoogle(GoogleAuthRequest(idToken, role))
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                persistAuthSession(authBody, role)
                Result.success(authBody)
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                Result.failure(
                    IllegalArgumentException(
                        if (errorBody.isNotBlank()) errorBody else "No se pudo validar el token de Google."
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loginAsDemo(role: UserRole, customEmail: String? = null): Boolean {
        sessionManager.saveAuthToken("demo_jwt_token")
        sessionManager.saveUserRole(role)
        if (role == UserRole.PATIENT) {
            sessionManager.saveUserId("usr_101")
            sessionManager.saveUserEmail(customEmail ?: "paciente@odontosystem.com")
            sessionManager.saveUserName("Nicole De La Cruz")
        } else {
            sessionManager.saveUserId("dnt_501")
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

    private fun persistAuthSession(authBody: AuthResponse, role: UserRole) {
        sessionManager.saveAuthToken(authBody.token)
        authBody.refreshToken?.let { sessionManager.saveRefreshToken(it) }
        sessionManager.saveUserId(authBody.user.id)
        sessionManager.saveUserEmail(authBody.user.email)
        sessionManager.saveUserName(authBody.user.name)
        sessionManager.saveUserRole(authBody.user.role)
    }
}
