package com.example.mycolloc.ui.auth

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mycolloc.R
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.mycolloc.databinding.DialogRegisterBinding
import com.example.mycolloc.viewmodels.AuthState
import com.example.mycolloc.viewmodels.AuthViewModel

class RegisterDialog : DialogFragment() {
    private var _binding: DialogRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

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

        setupViews()
        observeAuthState()
    }

    private fun setupViews() {
        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString()
            val phone = binding.etPhone.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // Validate input
            when {
                fullName.isBlank() -> showError("Full name is required")
                phone.isBlank() -> showError("Phone number is required")
                email.isBlank() -> showError("Email is required")
                password.isBlank() -> showError("Password is required")
                confirmPassword.isBlank() -> showError("Please confirm your password")
                password != confirmPassword -> showError("Passwords do not match")
                else -> {
                    // Split full name into first and last name
                    val nameParts = fullName.trim().split("\\s+".toRegex())
                    val firstName = nameParts.firstOrNull() ?: ""
                    val lastName = nameParts.drop(1).joinToString(" ")

                    viewModel.register(email, password, firstName, lastName, phone, requireActivity())
                }
            }
        }

        binding.tvLoginPrompt.setOnClickListener {
            dismiss()
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Registering..."
                }
                is AuthState.Authenticated -> {
                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                is AuthState.Error -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                    showError(state.message)
                }
                else -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 