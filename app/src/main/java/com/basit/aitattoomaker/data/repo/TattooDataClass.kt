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
    val device_id: String="",
    val created: Boolean=false,
    val access_token: String="",
    val refresh_token: String="",
    val expire_on: String=""
)

data class DeviceData(
    val deviceId: String="",
    val appName: String="",
    val deviceType: String="",
    val appVersion: String="",
    val modelName:String=""
)
data class ModelName(
    val device_model:String=""
)

data class DeviceProfile(val deviceId: String, val credits: Int)

data class DailyCreditsResponse(val claimed: Boolean, val credits: Int)


data class Transaction(val id: String, val amount: String, val date: String)

data class TokenRequest(val device_model: String)
data class TokenResponse(val access_token: String, val refresh_token: String)


data class PurchaseRequest(
    val subscription_plan_id: Int,
    val store_transaction_id: String,
    val purchase_amount: String
)


data class SubscriptionResponse(
    val meta: Meta,
    val response: ResponseData
)

data class ResponseData(
    val subscription_id: String,
    val subscription_plan: SubscriptionPlan,
    val started_at: String,
    val expires_at: String,
    val days_remaining: Int,
    val credits_added: Int,
    val new_credit_balance: Int,
    val purchase_amount: String,
    val status: String
)

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val duration: String,
    val duration_days: Int,
    val credits: Int
)
data class GenerationResponse(
    val meta: Meta,
    val response: GenerationData
)
data class GenerationData(
    val generation_id: String,
    val img_id: String,
    val status: String,
    val generation_type: String,
    val generated_img_url: String,
    val credits_remaining: Int,
    val credits_used: Int,
    val processing_time: String,
    val style: String,
    val original_prompt: String,
    val moderated_prompt: String
)
data class GenerationRequest(
    val style: String,
    val prompt: String,
    val gen_number: Int?=null,
    val dimensions: String?=null
)


