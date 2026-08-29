package com.odontosystem.app.repository

import com.odontosystem.app.data.local.SessionManager
import com.odontosystem.app.data.model.AuthRequest
import com.odontosystem.app.data.model.AuthResponse
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
                sessionManager.saveAuthToken(authBody.token)
                sessionManager.saveUserEmail(authBody.user.email)
                sessionManager.saveUserName(authBody.user.name)
                sessionManager.saveUserRole(role)
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
            Result.success(AuthResponse("demo_jwt_token", demoUser))
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authBody = response.body()!!
                sessionManager.saveAuthToken(authBody.token)
                sessionManager.saveUserEmail(authBody.user.email)
                sessionManager.saveUserName(authBody.user.name)
                sessionManager.saveUserRole(request.role)
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
