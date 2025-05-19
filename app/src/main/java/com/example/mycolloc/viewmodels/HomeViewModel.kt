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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


class HomeViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {
    private val _currentUserId = MutableLiveData<String?>()
    val currentUserId: LiveData<String?> = _currentUserId

    private val _offers = MutableLiveData<List<Offer>>()
    val offers: LiveData<List<Offer>> = _offers

    private val _nearbyOffers = MutableLiveData<List<Offer>>()
    val nearbyOffers: LiveData<List<Offer>> = _nearbyOffers

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState.Loading)
    val uiState: LiveData<HomeUiState> = _uiState

    private val _selectedOffer = MutableLiveData<Offer?>()
    val selectedOffer: LiveData<Offer?> = _selectedOffer

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    init {
        checkCurrentUser()
        loadOffers()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _currentUserId.value = repository.getCurrentUserId()
        }
    }

    private fun loadOffers() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val result = repository.getAllOffers()
                when (result) {
                    is Result.Success -> {
                        _uiState.value = HomeUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = HomeUiState.Error(result.exception.message ?: "Unknown error")
                    }
                    is Result.RecaptchaRequired -> {
                        // Handle reCAPTCHA requirement
                        _uiState.value = HomeUiState.Error("Please verify you are not a robot")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refreshOffers() {
        _isRefreshing.value = true
        loadOffers()
    }

    fun searchOffers(query: String) {
        if (query.isBlank()) {
            loadOffers()
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                when (val result = repository.searchOffers(query)) {
                    is Result.Success -> {
                        _uiState.value = HomeUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = HomeUiState.Error(
                            result.exception.message ?: "Search failed"
                        )
                    }
                    is Result.RecaptchaRequired -> {
                        // This shouldn't happen for searching offers
                        _uiState.value = HomeUiState.Error("Unexpected reCAPTCHA requirement")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun filterOffersByCategory(category: String) {
        if (category.isBlank()) {
            loadOffers()
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                when (val result = repository.getOffersByCategory(category)) {
                    is Result.Success -> {
                        _uiState.value = HomeUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = HomeUiState.Error(
                            result.exception.message ?: "Failed to filter offers"
                        )
                    }
                    is Result.RecaptchaRequired -> {
                        // This shouldn't happen for filtering offers
                        _uiState.value = HomeUiState.Error("Unexpected reCAPTCHA requirement")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Filtering failed")
            }
        }
    }

    fun filterOffersByPriceRange(minPrice: Double, maxPrice: Double) {
        if (minPrice < 0 || maxPrice < minPrice) {
            _uiState.value = HomeUiState.Error("Invalid price range")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                when (val result = repository.getOffersByPriceRange(minPrice, maxPrice)) {
                    is Result.Success -> {
                        _uiState.value = HomeUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = HomeUiState.Error(
                            result.exception.message ?: "Failed to filter offers"
                        )
                    }
                    is Result.RecaptchaRequired -> {
                        // This shouldn't happen for filtering offers
                        _uiState.value = HomeUiState.Error("Unexpected reCAPTCHA requirement")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Filtering failed")
            }
        }
    }

    fun filterOffersByLocation(latitude: Double, longitude: Double, radiusInKm: Double) {
        if (radiusInKm <= 0) {
            _uiState.value = HomeUiState.Error("Invalid radius")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                when (val result = repository.getOffersByLocation(latitude, longitude, radiusInKm)) {
                    is Result.Success -> {
                        _uiState.value = HomeUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = HomeUiState.Error(
                            result.exception.message ?: "Failed to filter offers by location"
                        )
                    }
                    is Result.RecaptchaRequired -> {
                        // This shouldn't happen for location filtering
                        _uiState.value = HomeUiState.Error("Unexpected reCAPTCHA requirement")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Location filtering failed")
            }
        }
    }

    fun selectOffer(offer: Offer) {
        _selectedOffer.value = offer
    }

    fun clearSelectedOffer() {
        _selectedOffer.value = null
    }

    fun deleteOffer(offerId: String) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteOffer(offerId)) {
                    is Result.Success -> {
                        // Refresh the offers list after successful deletion
                        loadOffers()
                    }
                    is Result.Error -> {
                        _uiState.value = HomeUiState.Error(
                            result.exception.message ?: "Failed to delete offer"
                        )
                    }
                    is Result.RecaptchaRequired -> {
                        // This shouldn't happen for deleting offers
                        _uiState.value = HomeUiState.Error("Unexpected reCAPTCHA requirement")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to delete offer")
            }
        }
    }

    fun updateOfferStatus(offerId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                when (val result = repository.updateOfferStatus(offerId, isActive)) {
                    is Result.Success -> {
                        // Refresh the offers list after successful update
                        loadOffers()
                    }
                    is Result.Error -> {
                        _uiState.value = HomeUiState.Error(
                            result.exception.message ?: "Failed to update offer status"
                        )
                    }
                    is Result.RecaptchaRequired -> {
                        // This shouldn't happen for updating offer status
                        _uiState.value = HomeUiState.Error("Unexpected reCAPTCHA requirement")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to update offer status")
            }
        }
    }

    fun createOffer(offer: Offer) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            when (val result = repository.createOffer(offer)) {
                is Result.Success -> {
                    loadOffers() // Reload offers after creating a new one
                }
                is Result.Error -> {
                    _uiState.value = HomeUiState.Error(result.exception.message ?: "Failed to create offer")
                }
                is Result.RecaptchaRequired -> {
                    // Since this is just creating an offer, reCAPTCHA shouldn't be required
                    _uiState.value = HomeUiState.Error("Unexpected reCAPTCHA requirement")
                }
            }
        }
    }

    fun loadNearbyOffers(latitude: Double, longitude: Double, radiusInKm: Double = 5.0) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.getNearbyOffers(latitude, longitude, radiusInKm).collect { offersList ->
                _nearbyOffers.value = offersList
                _uiState.value = HomeUiState.Success(offersList)
            }
        }
    }

    fun isUserAdmin(): Boolean {
        return _currentUser.value?.role == UserRole.ADMIN
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
} 