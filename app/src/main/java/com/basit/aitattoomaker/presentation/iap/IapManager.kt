package com.basit.aitattoomaker.presentation.iap

import android.content.Context
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.presentation.iap.inapppurchases.IapConnector

object IapManager {
    private var iapConnector: IapConnector? = null
    const val skuKeyWeekly = "weekly_subscription"
    const val skuKeyMonthly = "monthly_subscription"
//    const val skuKeyYearly = "yearly_subscription"
    fun getIapConnector(context: Context): IapConnector {
        return if (iapConnector != null) {
            iapConnector as IapConnector
        } else {
            val nonConsumablesList = listOf("")
            val subsList = listOf(
                skuKeyWeekly,
                skuKeyMonthly)
            val consumablesList = listOf("")
            IapConnector(
                context = context,
                nonConsumableKeys = nonConsumablesList,
                consumableKeys = consumablesList,
                subscriptionKeys = subsList,
                key = context.getString(R.string.licenseKey),
                enableLogging = true
            )
        }
    }
}