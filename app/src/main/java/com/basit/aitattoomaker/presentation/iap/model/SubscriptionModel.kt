package com.basit.aitattoomaker.presentation.iap.model

import androidx.annotation.Keep

@Keep
data class SubscriptionModel(
    val subscription_plan_id:Int?=1,
    val store_transaction_id:String?="",
    val purchase_amount:Double?=0.0,
)
