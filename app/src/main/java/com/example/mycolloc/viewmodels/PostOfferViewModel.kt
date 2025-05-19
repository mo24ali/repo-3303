package com.example.mycolloc.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycolloc.model.Offer
import com.example.mycolloc.model.Location
import com.example.mycolloc.repository.FirebaseRepository
import com.example.mycolloc.repository.Result
import kotlinx.coroutines.launch

sealed class PostOfferUiState {
    object Loading : PostOfferUiState()
    data class Success(val offerId: String) : PostOfferUiState()
    data class Error(val message: String) : PostOfferUiState()
}

class PostOfferViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _uiState = MutableLiveData<PostOfferUiState>()
    val uiState: LiveData<PostOfferUiState> = _uiState

    fun createOffer(
        title: String,
        description: String,
        price: Double,
        category: String,
        location: Location
    ) {
        viewModelScope.launch {
            _uiState.value = PostOfferUiState.Loading
            
            // Validate input
            if (title.isBlank() || description.isBlank() || price <= 0 || category.isBlank()) {
                _uiState.value = PostOfferUiState.Error("Please fill all required fields")
                return@launch
            }

            val offer = Offer(
                title = title,
                description = description,
                price = price,
                category = category,
                location = location.address,
                latitude = location.latitude,
                longitude = location.longitude,
                isActive = true
            )

            when (val result = repository.createOffer(offer)) {
                is Result.Success -> {
                    _uiState.value = PostOfferUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = PostOfferUiState.Error(result.exception.message ?: "Failed to create offer")
                }
                is Result.RecaptchaRequired -> {
                    // Since this is just creating an offer, reCAPTCHA shouldn't be required
                    _uiState.value = PostOfferUiState.Error("Unexpected reCAPTCHA requirement")
                }
            }
        }
    }
} 