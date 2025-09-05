package com.basit.aitattoomaker.presentation.iap.model

data class CustomInAppModel(
        val id: Int,
        val durationPlan: String,
        val description: String,
        val totalPrice: String,
        val discountedPrice: String,
        val discountPercent: String,
        val showDiscount: Boolean,
        val monthlyPrice:Double=0.0,
        val pricePerWeek : String = "",
        var freeTrial: String = ""
)
