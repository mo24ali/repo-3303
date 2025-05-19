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

class PostOfferViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _uiState = MutableLiveData<PostOfferUiState>()
    val uiState: LiveData<PostOfferUiState> = _uiState

    fun createOffer(
        title: String,
        description: String,
        price: Double,
        location: Location
    ) {
        if (title.isBlank() || description.isBlank() || price <= 0) {
            _uiState.value = PostOfferUiState.Error("Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = PostOfferUiState.Loading
            
            val offer = Offer(
                title = title,
                description = description,
                price = price,
                location = location
            )

            when (val result: Result<String> = repository.createOffer(offer)) {
                is Result.Success -> {
                    _uiState.value = PostOfferUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = PostOfferUiState.Error(result.exception.message ?: "Failed to create offer")
                }
            }
        }
    }
}

sealed class PostOfferUiState {
    object Loading : PostOfferUiState()
    data class Success(val offerId: String) : PostOfferUiState()
    data class Error(val message: String) : PostOfferUiState()
} 