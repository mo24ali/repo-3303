package com.example.mycolloc.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycolloc.repository.FirebaseRepository
import com.example.mycolloc.repository.Result
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _currentUserId = MutableLiveData<String?>()
    val currentUserId: LiveData<String?> = _currentUserId

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _currentUserId.value = repository.getCurrentUserId()
            _authState.value = if (_currentUserId.value != null) {
                AuthState.Authenticated(_currentUserId.value!!)
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result: Result<String> = repository.signIn(email, password)) {
                is Result.Success -> {
                    _currentUserId.value = result.data
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.exception.message ?: "Authentication failed")
                }
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result: Result<String> = repository.register(email, password)) {
                is Result.Success -> {
                    _currentUserId.value = result.data
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.exception.message ?: "Registration failed")
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _currentUserId.value = null
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
} 