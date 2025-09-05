package com.basit.aitattoomaker.presentation.iap.inapppurchases

interface BillingServiceListener {
    /**
     * Callback will be triggered upon obtaining information about product prices
     *
     * @param iapKeyPrices - a map with available products
     */
    fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>)
}