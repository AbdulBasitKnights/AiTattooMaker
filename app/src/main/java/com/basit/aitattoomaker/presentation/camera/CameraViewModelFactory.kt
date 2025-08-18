package com.basit.aitattoomaker.presentation.camera

import android.app.Application
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.basit.aitattoomaker.data.repo.TattooRepository

class CameraViewModelFactory(
    private val application: Application,
    private val tattooRepository: TattooRepository
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @OptIn(ExperimentalGetImage::class)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(application, tattooRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}