package com.example.mycolloc.ui.auth

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mycolloc.R
import com.example.mycolloc.databinding.DialogRegisterBinding
import com.example.mycolloc.viewmodels.AuthViewModel
import com.example.mycolloc.viewmodels.AuthState
import com.google.android.material.snackbar.Snackbar

class RegisterDialog : DialogFragment() {
    private var _binding: DialogRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Theme_ColoColo_Dialog).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                val email = binding.emailInput.editText?.text.toString()
                val password = binding.passwordInput.editText?.text.toString()
                val name = binding.nameInput.editText?.text.toString()
                val phone = binding.phoneInput.editText?.text.toString()
                viewModel.register(email, password, name, phone)
            }
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    showLoading(true)
                    enableInputs(false)
                }
                is AuthState.SignedIn, is AuthState.Authenticated -> {
                    showLoading(false)
                    dismiss()
                }
                is AuthState.SignedOut, is AuthState.Unauthenticated -> {
                    showLoading(false)
                    enableInputs(true)
                }
                is AuthState.Error -> {
                    showLoading(false)
                    enableInputs(true)
                    showError(state.message)
                }
                is AuthState.RecaptchaRequired -> {
                    showLoading(false)
                    enableInputs(true)
                    showReCAPTCHAError()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.btnRegister.isEnabled = !show
    }

    private fun enableInputs(enable: Boolean) {
        binding.emailInput.isEnabled = enable
        binding.passwordInput.isEnabled = enable
        binding.nameInput.isEnabled = enable
        binding.phoneInput.isEnabled = enable
        binding.btnRegister.isEnabled = enable
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showReCAPTCHAError() {
        showError("Please verify you are not a robot")
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        with(binding) {
            val email = emailInput.editText?.text.toString()
            val password = passwordInput.editText?.text.toString()
            val name = nameInput.editText?.text.toString()
            val phone = phoneInput.editText?.text.toString()

            if (email.isBlank()) {
                emailInput.error = getString(R.string.error_required)
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = getString(R.string.error_invalid_email)
                isValid = false
            } else {
                emailInput.error = null
            }

            if (password.isBlank()) {
                passwordInput.error = getString(R.string.error_required)
                isValid = false
            } else if (password.length < 6) {
                passwordInput.error = getString(R.string.error_invalid_password)
                isValid = false
            } else {
                passwordInput.error = null
            }

            if (name.isBlank()) {
                nameInput.error = getString(R.string.error_required)
                isValid = false
            } else {
                nameInput.error = null
            }

            if (phone.isBlank()) {
                phoneInput.error = getString(R.string.error_required)
                isValid = false
            } else {
                phoneInput.error = null
            }
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 