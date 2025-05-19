package com.example.mycolloc.viewmodels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycolloc.model.User
import com.example.mycolloc.repository.FirebaseRepository
import com.example.mycolloc.repository.Result
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val auth = Firebase.auth

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                when (val result = repository.getCurrentUser()) {
                    is Result.Success -> {
                        _currentUser.value = result.data
                        _authState.value = AuthState.Authenticated
                    }
                    is Result.Error -> {
                        _currentUser.value = null
                        _authState.value = AuthState.Unauthenticated
                    }
                    is Result.RecaptchaRequired -> {
                        _authState.value = AuthState.RecaptchaRequired
                    }
                }
            } catch (e: Exception) {
                _currentUser.value = null
                _authState.value = AuthState.Error("Failed to check authentication state")
            }
        }
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        activity: Activity
    ) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                // Quick validation
                if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank() || phoneNumber.isBlank()) {
                    _authState.value = AuthState.Error("All fields are required")
                    return@launch
                }

                // Password strength check
                if (password.length < 6) {
                    _authState.value = AuthState.Error("Password must be at least 6 characters")
                    return@launch
                }

                when (val result = repository.register(email, password, firstName, lastName, phoneNumber, activity)) {
                    is Result.Success -> {
                        // After successful registration, get the user data
                        when (val userResult = repository.getCurrentUser()) {
                            is Result.Success -> {
                                _currentUser.value = userResult.data
                                _authState.value = AuthState.Authenticated
                            }
                            is Result.Error -> {
                                _authState.value = AuthState.Error("Registration successful but failed to get user data")
                            }
                            is Result.RecaptchaRequired -> {
                                _authState.value = AuthState.RecaptchaRequired
                            }
                        }
                    }
                    is Result.Error -> {
                        val errorMessage = when (result.exception) {
                            is FirebaseAuthException -> {
                                when ((result.exception as FirebaseAuthException).errorCode) {
                                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email is already registered"
                                    "ERROR_INVALID_EMAIL" -> "Invalid email format"
                                    "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                                    else -> "Registration failed: ${result.exception.message}"
                                }
                            }
                            else -> "Registration failed: ${result.exception.message}"
                        }
                        _authState.value = AuthState.Error(errorMessage)
                    }
                    is Result.RecaptchaRequired -> {
                        _authState.value = AuthState.RecaptchaRequired
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun signIn(email: String, password: String, activity: Activity) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                // Quick validation
                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Email and password are required")
                    return@launch
                }

                when (val result = repository.signIn(email, password, activity)) {
                    is Result.Success -> {
                        _currentUser.value = result.data
                        _authState.value = AuthState.Authenticated
                    }
                    is Result.Error -> {
                        val errorMessage = when (result.exception) {
                            is FirebaseAuthException -> {
                                when ((result.exception as FirebaseAuthException).errorCode) {
                                    "ERROR_INVALID_EMAIL" -> "Invalid email format"
                                    "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                                    "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                                    "ERROR_USER_DISABLED" -> "This account has been disabled"
                                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later"
                                    else -> "Sign in failed: ${result.exception.message}"
                                }
                            }
                            else -> "Sign in failed: ${result.exception.message}"
                        }
                        _authState.value = AuthState.Error(errorMessage)
                    }
                    is Result.RecaptchaRequired -> {
                        _authState.value = AuthState.RecaptchaRequired
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sign in failed: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to sign out: ${e.message}")
            }
        }
    }

    fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = repository.updateUserProfile(firstName, lastName, phoneNumber)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Authenticated
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.exception.message ?: "Failed to update profile")
                }
                is Result.RecaptchaRequired -> {
                    // Since this is a profile update, reCAPTCHA shouldn't be required
                    // But we need to handle it to make the when expression exhaustive
                    _authState.value = AuthState.Error("Unexpected reCAPTCHA requirement during profile update")
                }
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object RecaptchaRequired : AuthState()
    data class Error(val message: String) : AuthState()
} 