package com.example.mycolloc.viewmodels

import com.example.mycolloc.model.Offer

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val offers: List<Offer> = emptyList()) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
} 