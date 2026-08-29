package com.odontosystem.app.data.model

enum class UserRole {
    PATIENT, DENTIST
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.PATIENT,
    val phone: String = "+51987654321"
)

data class AuthRequest(
    val email: String,
    val password: String,
    val role: UserRole = UserRole.PATIENT
)

data class AuthResponse(
    val token: String,
    val user: User
)
