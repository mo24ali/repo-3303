package com.example.mycolloc.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycolloc.data.local.Location
import com.example.mycolloc.model.Offer
import com.example.mycolloc.repository.FirebaseRepository
import com.example.mycolloc.repository.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

sealed class PostOfferUiState {
    object Loading : PostOfferUiState()
    data class Success(val offerId: String) : PostOfferUiState()
    data class Error(val message: String) : PostOfferUiState()
}

class PostOfferViewModel : ViewModel() {

    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableLiveData<PostOfferUiState>()
    val uiState: LiveData<PostOfferUiState> = _uiState

    fun createOffer(
        title: String,
        description: String,
        price: Double,
        category: String,
        location: Location, // ✅ objet Location correct
        images: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = PostOfferUiState.Loading

            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.value = PostOfferUiState.Error("User not authenticated")
                return@launch
            }

            // Vérification basique
            if (title.isBlank() || description.isBlank() || price <= 0 || category.isBlank()) {
                _uiState.value = PostOfferUiState.Error("Please fill all required fields")
                return@launch
            }

            // ✅ Création de l’offre avec Location complet
            val offer = Offer(
                userId = currentUser.uid,
                title = title,
                description = description,
                price = price,
                category = category,
                location = location, // ✅ bon type
                latitude = location.latitude ?: 0.0,
                longitude = location.longitude ?: 0.0,

                images = images,
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
                    _uiState.value = PostOfferUiState.Error("Unexpected reCAPTCHA requirement")
                }
            }
        }
    }
}
