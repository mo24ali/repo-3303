package com.example.mycolloc.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycolloc.model.Offer
import com.example.mycolloc.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailsViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState

    fun loadOffer(offerId: String) {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            try {
                // TODO: Implement offer loading from repository
                // For now, we'll just emit an error state
                _uiState.value = DetailsUiState.Error("Not implemented yet")
            } catch (e: Exception) {
                _uiState.value = DetailsUiState.Error(e.message ?: "Failed to load offer")
            }
        }
    }

    fun createNegotiation(offerId: String, amount: Double, message: String) {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            try {
                // TODO: Implement negotiation creation
                // For now, we'll just emit an error state
                _uiState.value = DetailsUiState.Error("Not implemented yet")
            } catch (e: Exception) {
                _uiState.value = DetailsUiState.Error(e.message ?: "Failed to create negotiation")
            }
        }
    }
}

sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(val offer: Offer) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
} 