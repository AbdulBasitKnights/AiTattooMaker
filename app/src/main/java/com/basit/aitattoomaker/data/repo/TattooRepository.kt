package com.basit.aitattoomaker.data.repo

import android.graphics.Bitmap
import android.net.Uri
import com.basit.aitattoomaker.domain.Tattoo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class TattooRepository @Inject constructor(private val api: TattooApiService) {
    suspend fun register(deviceData: DeviceData,modelName: ModelName): RegisterResponse {
            api.register(deviceData.deviceId,deviceData.appName,deviceData.deviceType,deviceData.appVersion,modelName)
        return api.register(deviceData.deviceId,deviceData.appName,deviceData.deviceType,deviceData.appVersion,modelName)
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