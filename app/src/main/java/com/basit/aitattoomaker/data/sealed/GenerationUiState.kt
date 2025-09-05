package com.basit.aitattoomaker.data.sealed

import com.basit.aitattoomaker.data.repo.GenerationResponse

sealed class GenerationUiState {
    object Idle : GenerationUiState()
    object Loading : GenerationUiState()
    data class Success(val data: GenerationResponse) : GenerationUiState()
    data class Error(val message: String) : GenerationUiState()
}
