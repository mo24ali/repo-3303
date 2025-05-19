package com.example.mycolloc.viewmodels

sealed class AuthState {
    object Loading : AuthState()
    object SignedIn : AuthState()
    object SignedOut : AuthState()
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object RecaptchaRequired : AuthState()
    data class Error(val message: String) : AuthState()
}
