package com.basit.aitattoomaker.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basit.aitattoomaker.data.repo.DailyCreditsResponse
import com.basit.aitattoomaker.data.repo.DeviceProfile
import com.basit.aitattoomaker.data.repo.TattooApiService
import com.basit.aitattoomaker.data.repo.TattooRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: TattooRepository

) : ViewModel() {

    private val _profile = MutableLiveData<DeviceProfile>()
    val profile: LiveData<DeviceProfile> = _profile

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _profile.value = repo.getProfile()
            } catch (e: Exception) {
                Log.e("VM", "Error loading profile", e)
            }
        }
    }

    fun claimDailyCredits(onResult: (DailyCreditsResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repo.claimCredits()
                onResult(res)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}
