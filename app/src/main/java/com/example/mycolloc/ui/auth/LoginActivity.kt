package com.example.mycolloc.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mycolloc.R
import com.example.mycolloc.databinding.ActivityLoginBinding
import com.example.mycolloc.ui.admin.AdminDashboardActivity
import com.example.mycolloc.ui.home.HomeActivity
import com.example.mycolloc.viewmodels.AuthState
import com.example.mycolloc.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private  var loadingDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeAuthState()
    }

    private fun setupViews() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            if (email == "admin@admin.com" && password == "admin") {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return@setOnClickListener
            }

            if (validateInput(email, password)) {
                viewModel.signIn(email, password)
            }
        }

        binding.signUpLink.setOnClickListener {
            startActivity(Intent(this,SignupActivity::class.java))
        }
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this,ForgetPasswordActivity::class.java))
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading(true)
                is AuthState.Authenticated -> {
                    showLoading(false)
                    navigateToHome()
                }
                is AuthState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is AuthState.Unauthenticated -> showLoading(false)
                else -> {}
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }


    private fun showLoading(show: Boolean) {
        if (show) {
            if (loadingDialog == null) {
                val builder = AlertDialog.Builder(this)
                val view = layoutInflater.inflate(R.layout.dialog_loading, null)
                builder.setView(view)
                builder.setCancelable(false)
                loadingDialog = builder.create()
            }
            loadingDialog?.show()
        } else {
            loadingDialog?.dismiss()
        }

        // désactiver les actions pendant le chargement
        binding.loginButton.isEnabled = !show
        binding.emailEditText.isEnabled = !show
        binding.passwordEditText.isEnabled = !show
        binding.signUpLink.isEnabled = !show
    }


    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
} 