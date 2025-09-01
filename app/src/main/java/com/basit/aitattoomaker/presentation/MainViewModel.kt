package com.basit.aitattoomaker.presentation

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basit.aitattoomaker.data.repo.DailyCreditsResponse
import com.basit.aitattoomaker.data.repo.DeviceData
import com.basit.aitattoomaker.data.repo.DeviceProfile
import com.basit.aitattoomaker.data.repo.ModelName
import com.basit.aitattoomaker.data.repo.RegisterResponse
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
    private val _registerResponse = MutableLiveData<RegisterResponse>()
    val registerResponse: LiveData<RegisterResponse> = _registerResponse
    init {
        registerUser()
    }
    fun registerUser() {
        viewModelScope.launch {
            try {
                val response = repo.register(DeviceData("1234567890","tattoo","android","1.0.1"),
                    ModelName("Samsung"))
                if (response.meta.code == 200) {
                    Log.d("VM", "User is Registered")
                    // Successful response, the device is already registered
                    if (response.response.created) {
                        // Device was successfully created
                        // Handle device created logic
                    } else {
                        // Device already registered, show appropriate message
                        Log.w("VM", "Already Registered")
                    }
                } else {
                    Log.e("VM", "Error registering user"+response.meta.message)
                }
            } catch (e: Exception) {
                Log.e("VM", "Error registering user", e)
                // Handle network error or other exceptions
                e.printStackTrace()
            }
        }
    }
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
