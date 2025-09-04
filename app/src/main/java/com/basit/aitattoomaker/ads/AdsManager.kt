package com.basit.aitattoomaker.ads

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.data.repo.NetworkUtils
import com.basit.aitattoomaker.presentation.utils.FirebaseEvents
import com.basit.aitattoomaker.presentation.utils.FirebaseEvents.extractMediatedNetworkName
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.drawable.toDrawable
import com.google.android.gms.ads.AdError

object AdsManager {
    var bannerAdRequest: AdRequest? = null
    var splashInterstitialAd: InterstitialAd? = null
    var loadingdialog:Dialog?=null
    var inter_bf_home_hf: InterstitialAd? = null
    var inter_bf_home: InterstitialAd? = null
    var inter_af_home_hf: InterstitialAd? = null
    var inter_af_home: InterstitialAd? = null
    var nativeAd: NativeAd? = null
    var isShowingAd = false
    val isPremiumSubscription: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    /**Banner**/
    fun loadBannerAd(adView: AdView, fragmentActivity: FragmentActivity) {
        try {
            if (isPremiumSubscription.value != true && NetworkUtils.isOnline(fragmentActivity)) {
                if (bannerAdRequest == null)
                    bannerAdRequest = AdRequest.Builder().build()
                bannerAdRequest?.let { bannerAdRequest->
                    adView.visibility = View.VISIBLE
                    adView.loadAd(bannerAdRequest)
                }
            } else {
                adView.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /**Interstitial After Home Ads**/
    fun loadInterstitialAdAfterSplash(
        activity: FragmentActivity,
        highFloorAd: String,
        normalAd: String,
        onAdLoaded: () -> Unit,
        onAdFailed: () -> Unit,
        onNoInternetorPro: () -> Unit,
    ) {
        if (isPremiumSubscription.value != true && NetworkUtils.isOnline(activity)) {
            var callbackCalled = false
            fun safeCallback(block: () -> Unit) {
                if (!callbackCalled) {
                    callbackCalled = true
                    block()
                }
            }
            fun loadSecond() {
                if(inter_af_home_hf==null){
                    val adRequest = AdRequest.Builder().build()
                    InterstitialAd.load(activity, normalAd, adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(ad: InterstitialAd) {
                                inter_af_home = ad
                                safeCallback { onAdLoaded() }
                                // show immediately if splash
                                inter_af_home?.show(activity)
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                safeCallback { onAdFailed() }
                            }
                        })
                }

            }
            if(inter_af_home_hf==null && inter_af_home==null){
                val adRequest = AdRequest.Builder().build()
                InterstitialAd.load(activity, highFloorAd, adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            inter_af_home_hf = ad
                            safeCallback { onAdLoaded() }

                            // show immediately if splash
                            inter_af_home_hf?.show(activity)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            loadSecond()
                        }
                    })
            }

        }
        else{
            onNoInternetorPro()
        }

    }
    fun showInterstitialAfterSplash(activity:FragmentActivity, ad: InterstitialAd, isHighFloor:Boolean, onAdFailed: () -> Unit, onAdShown: () -> Unit,
                               onAdDismissed: () -> Unit, onNoInternetorPro: () -> Unit) {
        if (isPremiumSubscription.value != true && NetworkUtils.isOnline(activity)) {
            showLoadingAdDialog(activity)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    loadingdialog?.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000)
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                        onAdShown()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        isShowingAd = false
                        inter_af_home=null
                        inter_af_home_hf=null
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isShowingAd = false
                        try {
                            loadingdialog?.dismiss()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        FirebaseEvents.firebaseUserAction(
                            "Splash",
                            if(isHighFloor)"ad_show_failed_splash_hf" else "ad_show_failed_splash")
                        onAdFailed()
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        isShowingAd = true
                        FirebaseEvents.firebaseUserAction(
                            "Splash",
                            if(isHighFloor)"ad_shown_splash_hf" else "ad_shown_splash")
                        ad?.setOnPaidEventListener {
                            val impressionData: AdValue = it
                            val data = SingularAdData(
                                "AdmobMediation_"+ extractMediatedNetworkName(splashInterstitialAd?.responseInfo?.mediationAdapterClassName),
                                impressionData.currencyCode,
                                impressionData.valueMicros / 1000000.0)
                            Singular.adRevenue(data)
                            Log.e("checkAdImpl","ImpressionData Inter: ${data}")
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1200)
                            try {
                                loadingdialog?.dismiss()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }
                }
            }
        }
        else{
           onNoInternetorPro()
        }
    }
    /**Interstitial Before Home Ads**/
    fun loadInterstitialAdSplash(
        activity: FragmentActivity,
        highFloorAd: String,
        normalAd: String,
        onAdLoaded: () -> Unit,
        onAdFailed: () -> Unit,
        onNoInternetorPro: () -> Unit,
    ) {
        if (isPremiumSubscription.value != true && NetworkUtils.isOnline(activity)) {
            var callbackCalled = false
            val startTime = System.currentTimeMillis()
            val timeoutMillis = 14_000L
            fun safeCallback(block: () -> Unit) {
                if (!callbackCalled) {
                    callbackCalled = true
                    block()
                }
            }

            fun loadSecond() {
                if (System.currentTimeMillis() - startTime > timeoutMillis) {
                    FirebaseEvents.firebaseUserAction(
                        "BeforeHome",
                        "inter_bf_home_req_timeout")
                    safeCallback { onAdFailed() }
                    return
                }
                if(inter_bf_home==null){
                    val adRequest = AdRequest.Builder().build()
                    InterstitialAd.load(activity, normalAd, adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(ad: InterstitialAd) {
                                inter_bf_home = ad
                                safeCallback { onAdLoaded() }
                                FirebaseEvents.firebaseUserAction(
                                    "BeforeHome",
                                    "inter_bf_home_req_suc")

                                // show immediately if splash
                                inter_bf_home?.show(activity)
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                FirebaseEvents.firebaseUserAction(
                                    "BeforeHome",
                                    "inter_bf_home_view_req_fail")
                                safeCallback { onAdFailed() }
                            }
                        })
                    FirebaseEvents.firebaseUserAction(
                        "BeforeHome",
                        "inter_bf_home_req")
                }

            }
            if(inter_af_home_hf==null && inter_bf_home==null){
                val adRequest = AdRequest.Builder().build()
                InterstitialAd.load(activity, highFloorAd, adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            inter_bf_home_hf = ad
                            FirebaseEvents.firebaseUserAction(
                                "BeforeHome",
                                "inter_bf_home_req_suc-hf")
                            safeCallback { onAdLoaded() }

                            // show immediately if splash
                            inter_bf_home_hf?.show(activity)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            FirebaseEvents.firebaseUserAction(
                                "BeforeHome",
                                "inter_bf_home_view_req_fail_hf")
                            loadSecond()
                        }
                    })
                FirebaseEvents.firebaseUserAction(
                    "BeforeHome",
                    "inter_bf_home_req_hf")
            }

        }
        else{
            onNoInternetorPro()
        }

    }
    fun showInterstitialSplash(activity:FragmentActivity, ad: InterstitialAd, isHighFloor:Boolean, onAdFailed: () -> Unit, onAdShown: () -> Unit,
                               onAdDismissed: () -> Unit, onNoInternetorPro: () -> Unit) {
        if (isPremiumSubscription.value != true && NetworkUtils.isOnline(activity)) {
            showLoadingAdDialog(activity)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    loadingdialog?.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000)
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                        onAdShown()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        isShowingAd = false
                        inter_bf_home=null
                        inter_bf_home_hf=null
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isShowingAd = false
                        try {
                            loadingdialog?.dismiss()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        FirebaseEvents.firebaseUserAction(
                            "Splash",
                            if(isHighFloor)"ad_show_failed_splash_hf" else "ad_show_failed_splash")
                        onAdFailed()
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        isShowingAd = true
                        FirebaseEvents.firebaseUserAction(
                            "Splash",
                            if(isHighFloor)"ad_shown_splash_hf" else "ad_shown_splash")
                        ad?.setOnPaidEventListener {
                            val impressionData: AdValue = it
                            val data = SingularAdData(
                                "AdmobMediation_"+ extractMediatedNetworkName(splashInterstitialAd?.responseInfo?.mediationAdapterClassName),
                                impressionData.currencyCode,
                                impressionData.valueMicros / 1000000.0)
                            Singular.adRevenue(data)
                            Log.e("checkAdImpl","ImpressionData Inter: ${data}")
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1200)
                            try {
                                loadingdialog?.dismiss()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }
                }
            }
        }
        else{
            onNoInternetorPro()
        }
    }
    fun showLoadingAdDialog(contextAct: Context): Dialog? {
        return try {
            val context = contextAct as Activity
            // Create and setup Dialog
            val dialog = Dialog(context).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(R.layout.loading_ad)
                setCancelable(false)

                window?.apply {
                    // Fullscreen layout
                    setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    setBackgroundDrawable(Color.BLACK.toDrawable())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Modern edge-to-edge approach
                        WindowCompat.setDecorFitsSystemWindows(this, false)

                        val controller = WindowCompat.getInsetsController(this, decorView)
                        controller?.isAppearanceLightStatusBars = true
                        statusBarColor = Color.BLACK
                    } else {
                        // Fallback for pre-API 30
                        decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        statusBarColor = Color.BLACK
                    }
                }
            }
            loadingdialog=dialog
            loadingdialog
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}