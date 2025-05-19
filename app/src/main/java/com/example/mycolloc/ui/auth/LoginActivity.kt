package com.example.mycolloc.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ActivityLoginBinding
import com.example.mycolloc.ui.home.HomeActivity
import com.example.mycolloc.viewmodels.AuthViewModel
import com.example.mycolloc.viewmodels.AuthState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.signIn(email, password)
        }

        binding.registerButton.setOnClickListener {
            showRegisterDialog()
        }

        binding.forgotPasswordButton.setOnClickListener {
            // TODO: Implement forgot password
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    showLoading(true)
                    disableInputs()
                }
                is AuthState.SignedIn, is AuthState.Authenticated -> {
                    showLoading(false)
                    navigateToHome()
                }
                is AuthState.SignedOut, is AuthState.Unauthenticated -> {
                    showLoading(false)
                    enableInputs()
                }
                is AuthState.Error -> {
                    showLoading(false)
                    enableInputs()
                    showError(state.message)
                }
                is AuthState.RecaptchaRequired -> {
                    showLoading(false)
                    enableInputs()
                    // TODO: Handle reCAPTCHA verification
                    showError("Please verify you are not a robot")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun enableInputs() {
        binding.emailEditText.isEnabled = true
        binding.passwordEditText.isEnabled = true
        binding.loginButton.isEnabled = true
        binding.registerButton.isEnabled = true
    }

    private fun disableInputs() {
        binding.emailEditText.isEnabled = false
        binding.passwordEditText.isEnabled = false
        binding.loginButton.isEnabled = false
        binding.registerButton.isEnabled = false
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showRegisterDialog() {
        RegisterDialog().show(supportFragmentManager, "register_dialog")
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
} 