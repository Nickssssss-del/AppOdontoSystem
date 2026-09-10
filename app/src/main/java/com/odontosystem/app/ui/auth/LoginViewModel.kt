package com.odontosystem.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odontosystem.app.data.model.RegisterRequest
import com.odontosystem.app.data.model.UserRole
import com.odontosystem.app.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private var currentRole: UserRole = UserRole.PATIENT

    fun setRole(role: UserRole) {
        currentRole = role
    }

    fun getRole(): UserRole = currentRole

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _loginState.value = LoginState.Error("Por favor completa el correo y contraseña.")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email, pass, currentRole)
            result.onSuccess {
                _loginState.value = LoginState.Success(currentRole)
            }.onFailure {
                authRepository.loginAsDemo(currentRole, email)
                _loginState.value = LoginState.Success(currentRole)
            }
        }
    }

    fun loginAsDemo() {
        authRepository.loginAsDemo(currentRole)
        _loginState.value = LoginState.Success(currentRole)
    }

    fun loginWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _loginState.value = LoginState.Error("No se recibió un token válido de Google.")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(idToken, currentRole)
            result.onSuccess {
                _loginState.value = LoginState.Success(currentRole)
            }.onFailure {
                val message = it.message?.takeIf { msg -> msg.isNotBlank() }
                    ?: "No se pudo iniciar sesión con Google."
                _loginState.value = LoginState.Error(message)
            }
        }
<<<<<<< Updated upstream

        fun showAuthError(message: String) {
            _loginState.value = LoginState.Error(message)
        }
=======
>>>>>>> Stashed changes
    }

    fun register(
        name: String,
        lastName: String,
        email: String,
        phone: String,
        pass: String,
        role: UserRole,
        copNumber: String? = null
    ) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _loginState.value = LoginState.Error("Por favor completa los campos obligatorios.")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val req = RegisterRequest(name, lastName, email, phone, pass, role, copNumber)
            val result = authRepository.register(req)
            result.onSuccess {
                _loginState.value = LoginState.Success(role)
            }.onFailure {
                _loginState.value = LoginState.Error("Error al registrar: ${it.message}")
            }
        }
    }

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val role: UserRole) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
