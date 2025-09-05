package com.basit.aitattoomaker.data.repo

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface TattooApiService {
    @POST("api/v1/devices/register/")
    suspend fun register(
        @Body modelName: ModelName
    ): Response<RegisterResponse>
    @POST("api/v1/devices/tokens/")
    suspend fun getToken(
        @Body modelName: ModelName
    ): Response<RegisterResponse>

    @GET("api/v1/devices/profile/")
    suspend fun getDeviceProfile(): DeviceProfile

    @GET("api/v1/devices/credits/daily-claims/")
    suspend fun claimDailyCredits(): DailyCreditsResponse

    @GET("api/v1/devices/subscriptions/plans/")
    suspend fun getSubscriptionPlans(@Header("Authorization") token: String?=null): List<SubscriptionPlan>

    @GET("api/v1/devices/credits/transactions/")
    suspend fun getTransactions(): List<Transaction>

    @POST("api/v1/devices/tokens/")
    suspend fun getTokens(@Body request: TokenRequest): TokenResponse

    @Multipart
    @POST("api/v1/devices/image/generate/")
    suspend fun generateImage(
        @Header("Authorization") token: String?=null,
        @Part("is_ref_design") isRef: RequestBody,
        @Part("room_type") roomType: RequestBody,
        @Part("style_type") styleType: RequestBody,
        @Part base_img: MultipartBody.Part
    ): GenerateImageResponse

    @POST("api/v1/devices/subscriptions/purchase/")
    suspend fun subscribe(@Header("Authorization") token: String?=null,
                          @Body request: PurchaseRequest): SubscriptionResponse


}