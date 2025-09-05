package com.basit.aitattoomaker.data.repo

import com.basit.aitattoomaker.presentation.utils.access_Token
import javax.inject.Inject

class TattooRepository @Inject constructor(private val api: TattooApiService) {
    suspend fun register(
        modelName: ModelName
    ): Result<RegisterResponse> {
        return try {
            val response = api.register(modelName)
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
        modelName: ModelName
    ): Result<RegisterResponse> {
        return try {
            val response = api.getToken(modelName)
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

    suspend fun getPlans(accessToken: String) = api.getSubscriptionPlans(accessToken)

    suspend fun getTransactions() = api.getTransactions()

    suspend fun getTokens(model: String) =
        api.getTokens(TokenRequest(model))

//    suspend fun generateImage(
//        isRef: Boolean,
//        roomType: String,
//        styleType: String,
//        file: File
//    ): GenerateImageResponse {
//        val requestFile = file.asRequestBody("image/*".toMediaType())
//        val body = MultipartBody.Part.createFormData("base_img", file.name, requestFile)
//
//        return api.generateImage(
//            isRef.toString().toRequestBody("text/plain".toMediaType()),
//            roomType.toRequestBody("text/plain".toMediaType()),
//            styleType.toRequestBody("text/plain".toMediaType()),
//            body
//        )
//    }

    suspend fun subscribe(req:PurchaseRequest) =
        api.subscribe(access_Token,req)


}