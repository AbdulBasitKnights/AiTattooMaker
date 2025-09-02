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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: TattooRepository

) : ViewModel() {

    private val _profile = MutableLiveData<DeviceProfile>()
    val profile: LiveData<DeviceProfile> = _profile
    init {
        registerUser("83d59106-102K", "interior", "ios", "1.2.3", ModelName("Samsung"))
    }
    private val _registerResponse = MutableLiveData<Result<RegisterResponse>>()
    val registerResponse: LiveData<Result<RegisterResponse>> get() = _registerResponse

    fun registerUser(deviceId: String, appName: String, deviceType: String, appVersion: String, modelName: ModelName) {
        viewModelScope.launch {
            val result = repo.register(deviceId, appName, deviceType, appVersion, modelName)
            _registerResponse.postValue(result)
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
