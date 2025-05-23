package com.example.mycolloc.viewmodels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycolloc.data.local.Location
import com.example.mycolloc.model.User

import com.example.mycolloc.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val repository = FirebaseRepository()

    private val _authState = MutableLiveData<AuthState>(AuthState.Unauthenticated)
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    // User is signed in, fetch user data
                    fetchUserData(firebaseUser.uid)
                } else {
                    _authState.value = AuthState.Unauthenticated
                    _currentUser.value = null
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to check authentication state: ${e.message}")
            }
        }
    }

    private suspend fun fetchUserData(userId: String) {
        try {
            val user = repository.getUser(userId)
            if (user != null) {
                _currentUser.value = user
                _authState.value = AuthState.Authenticated
            } else {
                // User document doesn't exist, sign out
                signOut()
                _authState.value = AuthState.Error("User data not found")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to fetch user data: ${e.message}")
        }
    }

    fun signIn(email: String, password: String) {
        if (!validateSignInInput(email, password)) return

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                withContext(Dispatchers.IO) {
                    auth.signInWithEmailAndPassword(email, password).await()
                }

                // ✅ Connexion réussie → mettre à jour enLigne = true
                auth.currentUser?.let { user ->
                    updateEnLigneStatus(user.uid, true)
                    fetchUserData(user.uid)
                }

            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    private fun updateEnLigneStatus(userId: String, isOnline: Boolean) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.child("enLigne").setValue(isOnline)
    }



    fun register(email: String, password: String, name: String, phone: String, location: Location) {
        if (!validateRegistrationInput(email, password, name, phone)) return

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                withContext(Dispatchers.IO) {
                    // Create user in Firebase Auth
                    val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                    val userId = authResult.user?.uid ?: throw IllegalStateException("User ID is null")

                    // Create user document in Firestore with location
                    val user = User(
                        id = userId,
                        email = email,
                        firstName = name.split(" ").firstOrNull() ?: name,
                        lastName = name.split(" ").drop(1).joinToString(" "),
                        phoneNumber = phone,
                        latitude = location.latitude ?: 0.0,
                        longitude = location.longitude ?: 0.0

                        )
                    repository.createUser(user)
                }
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }


    fun signOut() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid

                // ✅ 1. Mettre enLigne = false AVANT de se déconnecter
                if (userId != null) {
                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                    userRef.child("enLigne").setValue(false).await()
                }

                // ✅ 2. Déconnexion
                withContext(Dispatchers.IO) {
                    auth.signOut()
                }

                // ✅ 3. Réinitialiser l’état local
                _currentUser.value = null
                _authState.value = AuthState.SignedOut

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to sign out: ${e.message}")
            }
        }
    }


    fun resetPassword(email: String) {
        if (!validateEmail(email)) {
            _authState.value = AuthState.Error("Invalid email address")
            return
        }

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                withContext(Dispatchers.IO) {
                    auth.sendPasswordResetEmail(email).await()
                }
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    private fun handleAuthError(e: Exception) {
        _authState.value = when (e) {
            is FirebaseAuthInvalidUserException -> AuthState.Error("No account found with this email")
            is FirebaseAuthInvalidCredentialsException -> AuthState.Error("Invalid email or password")
            is FirebaseAuthWeakPasswordException -> AuthState.Error("Password is too weak")
            else -> AuthState.Error(e.message ?: "Authentication failed")
        }
    }

    private fun validateSignInInput(email: String, password: String): Boolean {
        when {
            email.isBlank() -> {
                _authState.value = AuthState.Error("Email is required")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _authState.value = AuthState.Error("Invalid email format")
                return false
            }
            password.isBlank() -> {
                _authState.value = AuthState.Error("Password is required")
                return false
            }
            password.length < 6 -> {
                _authState.value = AuthState.Error("Password must be at least 6 characters")
                return false
            }
        }
        return true
    }

    private fun validateRegistrationInput(email: String, password: String, name: String, phone: String): Boolean {
        when {
            !validateEmail(email) -> return false
            !validatePassword(password) -> return false
            name.isBlank() -> {
                _authState.value = AuthState.Error("Name is required")
                return false
            }
            phone.isBlank() -> {
                _authState.value = AuthState.Error("Phone number is required")
                return false
            }
            !validatePhone(phone) -> {
                _authState.value = AuthState.Error("Invalid phone number format")
                return false
            }
        }
        return true
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isBlank() -> {
                _authState.value = AuthState.Error("Email is required")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _authState.value = AuthState.Error("Invalid email format")
                false
            }
            else -> true
        }
    }

    private fun validatePassword(password: String): Boolean {
        return when {
            password.isBlank() -> {
                _authState.value = AuthState.Error("Password is required")
                false
            }
            password.length < 6 -> {
                _authState.value = AuthState.Error("Password must be at least 6 characters")
                false
            }
            !password.any { it.isDigit() } -> {
                _authState.value = AuthState.Error("Password must contain at least one number")
                false
            }
            !password.any { it.isUpperCase() } -> {
                _authState.value = AuthState.Error("Password must contain at least one uppercase letter")
                false
            }
            else -> true
        }
    }

    private fun validatePhone(phone: String): Boolean {
        // Basic phone validation - can be enhanced based on requirements
        val phoneRegex = "^\\+?[1-9]\\d{1,14}$".toRegex()
        return phoneRegex.matches(phone)
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
} 