package com.odontosystem.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.odontosystem.app.OdontoApplication
import com.odontosystem.app.data.model.UserRole
import com.odontosystem.app.data.remote.RetrofitClient
import com.odontosystem.app.databinding.ActivityLoginBinding
import com.odontosystem.app.repository.AuthRepository
import com.odontosystem.app.ui.dentist.DentistDashboardActivity
import com.odontosystem.app.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = (application as OdontoApplication).sessionManager
        val repository = AuthRepository(RetrofitClient.apiService, sessionManager)
        viewModel = LoginViewModel(repository)

        // Check if already logged in
        if (repository.isLoggedIn()) {
            navigateToDashboard(repository.getUserRole())
            return
        }

        setupRoleTabs()
        setupListeners()
        observeViewModel()
    }

    private fun setupRoleTabs() {
        binding.tabRoleSelector.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val role = if (tab?.position == 1) UserRole.DENTIST else UserRole.PATIENT
                viewModel.setRole(role)

                if (role == UserRole.DENTIST) {
                    binding.etEmail.setText("dr.ramos@odontosystem.com")
                } else {
                    binding.etEmail.setText("nicole@odontosystem.com")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            viewModel.login(email, pass)
        }

        binding.btnGuest.setOnClickListener {
            viewModel.loginAsDemo()
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is LoginViewModel.LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    navigateToDashboard(state.role)
                }
                is LoginViewModel.LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
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
