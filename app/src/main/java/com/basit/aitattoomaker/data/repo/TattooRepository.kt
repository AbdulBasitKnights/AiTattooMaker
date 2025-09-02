package com.basit.aitattoomaker.data.repo

import android.graphics.Bitmap
import android.net.Uri
import com.basit.aitattoomaker.domain.Tattoo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import javax.inject.Inject

class TattooRepository @Inject constructor(private val api: TattooApiService) {
    suspend fun register(
        deviceId: String,
        appName: String,
        deviceType: String,
        appVersion: String,
        modelName: ModelName
    ): Result<RegisterResponse> {
        return try {
            val response = api.register(deviceId, appName, deviceType, appVersion, modelName)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getToken(
        deviceId: String,
        appName: String,
        deviceType: String,
        appVersion: String,
        modelName: ModelName
    ): Result<RegisterResponse> {
        return try {
            val response = api.getToken(deviceId, appName, deviceType, appVersion, modelName)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile() = api.getDeviceProfile()

    suspend fun claimCredits() = api.claimDailyCredits()

    suspend fun getPlans() = api.getSubscriptionPlans()

    suspend fun getTransactions() = api.getTransactions()

    suspend fun getTokens(model: String) =
        api.getTokens(TokenRequest(model))

    suspend fun generateImage(
        isRef: Boolean,
        roomType: String,
        styleType: String,
        file: File
    ): GenerateImageResponse {
        val requestFile = file.asRequestBody("image/*".toMediaType())
        val body = MultipartBody.Part.createFormData("base_img", file.name, requestFile)

        return api.generateImage(
            isRef.toString().toRequestBody("text/plain".toMediaType()),
            roomType.toRequestBody("text/plain".toMediaType()),
            styleType.toRequestBody("text/plain".toMediaType()),
            body
        )
    }

    suspend fun subscribe(planId: Int, txnId: String, amount: String) =
        api.subscribe(PurchaseRequest(planId, txnId, amount))


}