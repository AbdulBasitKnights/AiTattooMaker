package com.basit.aitattoomaker.presentation.iap.inapppurchases

import com.android.billingclient.api.AccountIdentifiers

class DataWrappers {

    data class ProductDetails(
        val title: String?,
        val description: String?,
        val price: String?,
        val priceAmount: Double?,
        val priceCurrencyCode: String?,
        val billingCycleCount: Int?,
        val billingPeriod: String?,
        val recurrenceMode: Int?,
        val freeTrail : String,
        val offerPercentage:Int?=0,
        val discount:Int?=0
    )

    data class PurchaseInfo(
        val purchaseState: Int,
        val developerPayload: String,
        val isAcknowledged: Boolean,
        val isAutoRenewing: Boolean,
        val orderId: String?,
        val originalJson: String,
        val packageName: String,
        val purchaseTime: Long,
        val purchaseToken: String,
        val signature: String,
        val sku: String,
        val accountIdentifiers: AccountIdentifiers?
    )
}