package com.odontosystem.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.odontosystem.app.OdontoApplication
import com.odontosystem.app.data.model.UserRole
import com.odontosystem.app.data.remote.RetrofitClient
import com.odontosystem.app.repository.AuthRepository
import com.odontosystem.app.ui.dentist.DentistDashboardActivity
import com.odontosystem.app.ui.main.MainActivity

class LoginActivity : ComponentActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = (application as OdontoApplication).sessionManager
        val repository = AuthRepository(RetrofitClient.apiService, sessionManager)
        viewModel = LoginViewModel(repository)

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
                    onLoginSuccess = onLoginSuccess
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
}
