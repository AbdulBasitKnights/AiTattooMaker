package com.basit.aitattoomaker.presentation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.edit
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
import com.basit.aitattoomaker.extension.ACCESS_TOKEN_KEY
import com.basit.aitattoomaker.extension.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    private val _registerResponse = MutableLiveData<Result<RegisterResponse>>()
    val registerResponse: LiveData<Result<RegisterResponse>> get() = _registerResponse
    private val _getTokenResponse = MutableLiveData<Result<RegisterResponse>>()
    val getTokenResponse: LiveData<Result<RegisterResponse>> get() = _getTokenResponse

    fun registerUser(deviceId: String, appName: String, deviceType: String, appVersion: String, modelName: ModelName) {
        viewModelScope.launch {
            val result = repo.register(deviceId, appName, deviceType, appVersion, modelName)
            _registerResponse.postValue(result)
        }
    }
    fun getToken(deviceId: String, appName: String, deviceType: String, appVersion: String, modelName: ModelName) {
        viewModelScope.launch {
            val result = repo.getToken(deviceId, appName, deviceType, appVersion, modelName)
            _getTokenResponse.postValue(result)
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
    suspend fun storeAccessToken(context: Context, token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }
    fun getAccessToken(context: Context): Flow<String?> {
        return context.dataStore.data
            .map { preferences ->
                // Retrieve the access token from DataStore (return null if it doesn't exist)
                preferences[ACCESS_TOKEN_KEY]
            }
    }

    fun isAccessTokenAvailable(context: Context): Flow<Boolean> {
        return getAccessToken(context).map { token ->
            token != null
        }
    }


}
