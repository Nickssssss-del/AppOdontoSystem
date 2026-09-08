package com.odontosystem.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
    private val tag = "LoginActivity"

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
            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                Log.e(tag, "NoCredentialException en Google Sign-In", e)
                notifyAuthError("No hay cuentas de Google disponibles en este dispositivo.")
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                Log.e(tag, "Google Sign-In cancelado por el usuario", e)
                notifyAuthError("Inicio de sesión con Google cancelado por el usuario.")
            } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                Log.e(tag, "GetCredentialException en Google Sign-In", e)
                notifyAuthError("No se pudo completar Google Sign-In. Inténtalo nuevamente.")
            } catch (e: Exception) {
                Log.e(tag, "Error no controlado en Google Sign-In", e)
                notifyAuthError("Error inesperado al iniciar sesión con Google.")
            }
        }
    }

    private fun buildGoogleCredentialRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.google_web_client_id))
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
                Log.e(tag, "No se pudo parsear el ID token de Google", e)
                notifyAuthError("No se pudo leer el token de Google.")
            }
            return
        }

        Log.e(tag, "Credencial recibida no es GoogleIdTokenCredential: ${credential.type}")
        notifyAuthError("Credencial de Google no válida.")
    }

    private fun notifyAuthError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        viewModel.showAuthError(message)
    }
}
