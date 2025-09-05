package com.basit.aitattoomaker.presentation.iap.inapppurchases

interface BillingClientConnectionListener {
    fun onConnected(status: Boolean, billingResponseCode: Int)
}