package com.odontosystem.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import com.odontosystem.app.OdontoApplication
import com.odontosystem.app.R
import com.odontosystem.app.data.model.UserRole
import com.odontosystem.app.data.remote.RetrofitClient
import com.odontosystem.app.repository.AuthRepository
import com.odontosystem.app.ui.dentist.DentistDashboardActivity
import com.odontosystem.app.ui.main.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = (application as OdontoApplication).sessionManager
        val repository = AuthRepository(RetrofitClient.apiService, sessionManager)
        viewModel = LoginViewModel(repository)
        credentialManager = CredentialManager.create(this)

        // Check if already logged in
        if (repository.isLoggedIn()) {
            navigateToDashboard(repository.getUserRole())
            return
        }

        setContent {
            AuthenticationApp(viewModel) { role ->
                navigateToDashboard(role)
            }
        }
    }

    @Composable
    fun AuthenticationApp(viewModel: LoginViewModel, onLoginSuccess: (UserRole) -> Unit) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = onLoginSuccess,
                    onGoogleSignInClick = { startGoogleSignIn() }
                )
            }
            composable("register") {
                RegisterScreen(
                    navController = navController,
                    viewModel = viewModel,
                    onRegisterSuccess = onLoginSuccess
                )
            }
        }
    }

    private fun navigateToDashboard(role: UserRole) {
        val intent = if (role == UserRole.DENTIST) {
            Intent(this, DentistDashboardActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun startGoogleSignIn() {
        lifecycleScope.launch {
            val request = buildGoogleCredentialRequest()
            try {
                val result = credentialManager.getCredential(
                    this@LoginActivity,
                    request
                )
                handleGoogleCredential(result)
            } catch (e: Exception) {
                val className = e::class.java.simpleName
                when {
                    className.contains("Cancellation", ignoreCase = true) ->
                        viewModel.showAuthError("Inicio de sesión con Google cancelado por el usuario.")
                    className.contains("NoCredential", ignoreCase = true) ->
                        viewModel.showAuthError("No hay cuentas de Google disponibles en este dispositivo.")
                    else ->
                        viewModel.showAuthError("No se pudo completar Google Sign-In. Inténtalo nuevamente.")
                }
            }
        }
    }

    private fun buildGoogleCredentialRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.google_android_client_id))
            .setAutoSelectEnabled(false)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    private fun handleGoogleCredential(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                viewModel.loginWithGoogle(googleTokenCredential.idToken)
            } catch (e: GoogleIdTokenParsingException) {
                viewModel.showAuthError("No se pudo leer el token de Google.")
            }
            return
        }

        viewModel.showAuthError("Credencial de Google no válida.")
    }
}
