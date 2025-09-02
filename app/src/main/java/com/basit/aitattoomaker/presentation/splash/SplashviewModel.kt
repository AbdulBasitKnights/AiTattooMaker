package com.basit.aitattoomaker.presentation.splash

import android.app.Application
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.singular.sdk.SingularConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashviewModel @Inject constructor(
    private val application: Application,
    val singularConfig: SingularConfig
) : ViewModel() {
//    private lateinit var iapConnector: IapConnector

     init {
         initializeData()
       }
    private fun initializeData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
//                initIAP()
                initFireBase()
            } catch (e: Exception) {
//
            }
        }
    }
//    private fun initIAP() {
//        iapConnector = IapManager.getIapConnector(application.applicationContext)
//        iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
//            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
////                GlobalValues.isProVersion = true
//            }
//
//            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
//                when (purchaseInfo.sku) {
//                    IapManager.skuKeyMonthly -> {
////                        GlobalValues.isProVersion = true
//                    }
//
//                    IapManager.skuKeyYearly -> {
////                        GlobalValues.isProVersion = true
//                    }
//                }
//            }
//
//            override fun onPricesUpdated(iapKeyPrices: Map<String, List<DataWrappers.ProductDetails>>) {
//                // Handle price updates if needed
//            }
//        })
//    }
    private fun initFireBase() {
            try {
                if (FirebaseApp.getApps(application.applicationContext).isEmpty()) {
                    FirebaseApp.initializeApp(application.applicationContext)
                }
            } catch (e: Exception) {
                //
            }
    }
}