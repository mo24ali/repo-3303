package com.example.mycolloc.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycolloc.model.Offer
import com.example.mycolloc.model.User
import com.example.mycolloc.model.UserRole
import com.example.mycolloc.repository.FirebaseRepository
import com.example.mycolloc.repository.Result
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _currentUserId = MutableLiveData<String?>()
    val currentUserId: LiveData<String?> = _currentUserId

    private val _offers = MutableLiveData<List<Offer>>()
    val offers: LiveData<List<Offer>> = _offers

    private val _nearbyOffers = MutableLiveData<List<Offer>>()
    val nearbyOffers: LiveData<List<Offer>> = _nearbyOffers

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _uiState = MutableLiveData<HomeUiState>()
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        checkCurrentUser()
        loadOffers()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _currentUserId.value = repository.getCurrentUserId()
        }
    }

    fun loadOffers() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            when (val result: Result<List<Offer>> = repository.getOffers()) {
                is Result.Success -> {
                    _offers.value = result.data
                    _uiState.value = HomeUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = HomeUiState.Error(result.exception.message ?: "Failed to load offers")
                }
            }
        }
    }

    fun createOffer(offer: Offer) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            when (val result: Result<String> = repository.createOffer(offer)) {
                is Result.Success -> {
                    loadOffers() // Reload the offers list
                    _uiState.value = HomeUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = HomeUiState.Error(result.exception.message ?: "Failed to create offer")
                }
            }
        }
    }

    fun refreshOffers() {
        loadOffers()
    }

    fun loadNearbyOffers(latitude: Double, longitude: Double, radiusInKm: Double = 5.0) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.getNearbyOffers(latitude, longitude, radiusInKm).collect { offersList ->
                _nearbyOffers.value = offersList
                _uiState.value = HomeUiState.Success
            }
        }
    }

    fun deleteOffer(offerId: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.deleteOffer(offerId)
                .onSuccess {
                    // Refresh the offers list
                    loadOffers()
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(error.message ?: "Failed to delete offer")
                }
        }
    }

    fun isUserAdmin(): Boolean {
        return _currentUser.value?.role == UserRole.ADMIN
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Success : HomeUiState()
    data class Error(val message: String) : HomeUiState()
} 