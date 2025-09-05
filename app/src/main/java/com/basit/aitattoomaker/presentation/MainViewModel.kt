package com.basit.aitattoomaker.presentation

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basit.aitattoomaker.data.repo.DailyCreditsResponse
import com.basit.aitattoomaker.data.repo.DeviceProfile
import com.basit.aitattoomaker.data.repo.ModelName
import com.basit.aitattoomaker.data.repo.PurchaseRequest
import com.basit.aitattoomaker.data.repo.RegisterResponse
import com.basit.aitattoomaker.data.repo.SubscriptionPlan
import com.basit.aitattoomaker.data.repo.TattooRepository
import com.basit.aitattoomaker.data.sealed.SubscriptionUiState
import com.basit.aitattoomaker.extension.ACCESS_TOKEN_KEY
import com.basit.aitattoomaker.extension.dataStore
import com.basit.aitattoomaker.presentation.utils.access_Token
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val application: Application,
    private val repo: TattooRepository

) : ViewModel() {

    private val _profile = MutableLiveData<DeviceProfile>()
    val profile: LiveData<DeviceProfile> = _profile
    private val _registerResponse = MutableLiveData<Result<RegisterResponse>>()
    val registerResponse: LiveData<Result<RegisterResponse>> get() = _registerResponse
    private val _getTokenResponse = MutableLiveData<Result<RegisterResponse>>()
    val getTokenResponse: LiveData<Result<RegisterResponse>> get() = _getTokenResponse
    private val _subscriptionState = MutableLiveData<SubscriptionUiState>()
    val subscriptionState: LiveData<SubscriptionUiState> = _subscriptionState
    private val _getPlan = MutableLiveData<List<SubscriptionPlan>>()
    val getPlan: LiveData<List<SubscriptionPlan>> = _getPlan

    fun registerUser(modelName: ModelName) {
        viewModelScope.launch {
            isAccessTokenAvailable(application).collect { isAvailable ->
                if (isAvailable) {
                    // Access token exists
                    Log.d("VM","Access token already exists")
                    access_Token=getAccessToken(application).toString()
                    Log.d("VM","Access Token: ${access_Token.toString()}")

                } else {
                    // Token doesn't exist
                    viewModelScope.launch {
                        Log.e("VM","Request for Registration")
                        val result = repo.register(modelName)
                        _registerResponse.postValue(result)
                        result.onSuccess { registerResponse ->
                            Log.e("VM","Request for Token")
                            getToken( modelName)
                        }
                    } }
            }
        }

    }
    fun getToken( modelName: ModelName) {
        viewModelScope.launch {
            val result = repo.getToken(modelName)
            _getTokenResponse.postValue(result)
            result.onSuccess { registerResponse ->
                access_Token=registerResponse.response.access_token
                Log.e("VM","Token received")
                storeAccessToken(application, registerResponse.response.access_token)
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


    fun purchaseSubscription(
        planId: Int,
        transactionId: String,
        purchaseAmount: String,
    ) {
        viewModelScope.launch {
            _subscriptionState.value = SubscriptionUiState.Loading
            try {
                val request = PurchaseRequest(
                    subscription_plan_id = planId,
                    store_transaction_id = transactionId,
                    purchase_amount = purchaseAmount
                )
                val response = repo.subscribe(request)
                _subscriptionState.value = SubscriptionUiState.Success(response)

            } catch (e: Exception) {
                _subscriptionState.value = SubscriptionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    fun getSubPlan() {
        viewModelScope.launch {
            try {
                val response = repo.getPlans(access_Token?:"")
                _getPlan.value = response
            } catch (e: Exception) {
               e.printStackTrace()
            }
        }
    }

}
