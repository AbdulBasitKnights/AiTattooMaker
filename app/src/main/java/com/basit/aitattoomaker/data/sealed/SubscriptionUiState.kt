package com.basit.aitattoomaker.data.sealed

import com.basit.aitattoomaker.data.repo.SubscriptionResponse

sealed class SubscriptionUiState {
    object Loading : SubscriptionUiState()
    data class Success(val data: SubscriptionResponse) : SubscriptionUiState()
    data class Error(val message: String) : SubscriptionUiState()
}
