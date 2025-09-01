package com.basit.aitattoomaker.data.repo

data class RegisterRequest(val device_model: String)

data class RegisterResponse(
    val meta: Meta,
    val response: DeviceResponse
)

data class Meta(
    val code: Int,
    val status: String,
    val message: String
)

data class DeviceResponse(
    val device_id: String,
    val created: Boolean
)

data class DeviceData(
    val deviceId: String="",
    val appName: String="",
    val deviceType: String="",
    val appVersion: String="",
    val modelName:String=""
)
data class ModelName(
    val modelName:String=""
)

data class DeviceProfile(val deviceId: String, val credits: Int)

data class DailyCreditsResponse(val claimed: Boolean, val credits: Int)

data class SubscriptionPlan(val id: Int, val name: String, val price: String)

data class Transaction(val id: String, val amount: String, val date: String)

data class TokenRequest(val device_model: String)
data class TokenResponse(val access_token: String, val refresh_token: String)

data class GenerateImageResponse(val id: String, val imageUrl: String)

data class PurchaseRequest(
    val subscription_plan_id: Int,
    val store_transaction_id: String,
    val purchase_amount: String
)

data class PurchaseResponse(val success: Boolean, val message: String)
