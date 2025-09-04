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
import com.basit.aitattoomaker.presentation.utils.AppUtils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener

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
    fun loadBannerAd(
        adView: AdView,
        fragmentActivity: FragmentActivity,
        highFloorAdUnitId: String,
        normalAdUnitId: String
    ) {
        try {
            Log.d("BannerAd", "loadBannerAd")
            var secondRequest=false
            if (isPremiumSubscription.value != true && NetworkUtils.isOnline(fragmentActivity)) {
// set first ad id (high floor)
                adView.adUnitId = highFloorAdUnitId
                if (bannerAdRequest == null) {
                    bannerAdRequest = AdRequest.Builder().build()
                }

                adView.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("BannerAd", "Ad Loaded")
                        adView.visibility = View.VISIBLE
                        FirebaseEvents.firebaseUserAction("Download", "ban_image_dow_req_suc")
                        FirebaseEvents.firebaseUserAction("Download", "ban_image_dow_view")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        if(!secondRequest){
                            Log.e("BannerAd", "High floor failed: ${error.message}")
                            FirebaseEvents.firebaseUserAction("Download", "ban_image_dow_req_fail_hf")
                            FirebaseEvents.firebaseUserAction("Download", "ban_image_dow_req")
                            // retry with normal ad ID
                            adView.adUnitId = normalAdUnitId
                            adView.loadAd(bannerAdRequest!!)
                            secondRequest=true
                        }
                        else{
                            Log.e("BannerAd", "Normal failed: ${error.message}")
                            FirebaseEvents.firebaseUserAction("Download", "ban_image_dow_req_fail")
                        }

                    }
                }
                bannerAdRequest?.let { request ->
                    adView.visibility=View.VISIBLE
                    adView.loadAd(request)
                    FirebaseEvents.firebaseUserAction("Download", "ban_image_dow_req_hf")
                    Log.w("BannerAd", "High Floor Request:")
                }

            } else {
                adView.visibility = View.INVISIBLE
                Log.d("BannerAd", "Else")
            }
        } catch (e: Exception) {
            Log.d("BannerAd", "Exception: ${e.message}")
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
                                FirebaseEvents.firebaseUserAction(
                                    "AfterHome",
                                    "inter_after_home_req_suc")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                safeCallback { onAdFailed() }
                                FirebaseEvents.firebaseUserAction(
                                    "AfterHome",
                                    "inter_after_home_view_req_fail")

                            }
                        })
                    FirebaseEvents.firebaseUserAction(
                        "AfterHome",
                        "inter_after_home_req")
                }

            }
            if(inter_af_home_hf==null && inter_af_home==null){
                val adRequest = AdRequest.Builder().build()
                InterstitialAd.load(activity, highFloorAd, adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            inter_af_home_hf = ad
                            safeCallback { onAdLoaded() }
                            FirebaseEvents.firebaseUserAction(
                                "AfterHome",
                                "inter_after_home_req_suc_hf")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            loadSecond()
                            FirebaseEvents.firebaseUserAction(
                                "AfterHome",
                                "inter_after_home_view_req_fail_hf")
                        }
                    })
                FirebaseEvents.firebaseUserAction(
                    "AfterHome",
                    "inter_after_home_req_hf")
            }

        }
        else{
            onNoInternetorPro()
        }

    }
    fun showInterstitialAfterSplash(activity:FragmentActivity, ad: InterstitialAd?, isHighFloor:Boolean, onAdFailed: () -> Unit, onAdShown: () -> Unit,
                               onAdDismissed: () -> Unit, onNoInternetorPro: () -> Unit) {
        if (isPremiumSubscription.value != true && NetworkUtils.isOnline(activity) && ad!=null) {
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
                        loadInterstitialAdAfterSplash(activity,activity.resources.getString(R.string.inter_af_home_hf),activity.resources.getString(R.string.inter_af_home),{},{},{})
                        AppUtils.enableImmersiveMode(activity)
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
                            "AfterHome",
                            if(isHighFloor)"inter_after_home_show_fail_hf" else "inter_after_home_show_fail")
                        onAdFailed()
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        AppUtils.disableImmersiveMode(activity)
                        isShowingAd = true
                        FirebaseEvents.firebaseUserAction(
                            "AfterHome",
                            if(isHighFloor)"inter_after_home_view_hf" else "inter_after_home_view")
                        ad.setOnPaidEventListener {
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
                ad.show(activity)
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
                if (System.currentTimeMillis() - startTime > timeoutMillis) {
                    FirebaseEvents.firebaseUserAction(
                        "BeforeHome",
                        "inter_bf_home_req_timeout")
                    safeCallback { onAdFailed() }
                    return
                }
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
    fun showInterstitialSplash(activity:FragmentActivity, ad: InterstitialAd?, isHighFloor:Boolean, onAdFailed: () -> Unit, onAdShown: () -> Unit,
                               onAdDismissed: () -> Unit, onNoInternetorPro: () -> Unit) {
        if (isPremiumSubscription.value != true && NetworkUtils.isOnline(activity) && ad!=null) {
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
                            "BeforeHome",
                            if(isHighFloor)"inter_bf_home_fail_hf" else "inter_bf_home_fail")
                        onAdFailed()
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        isShowingAd = true
                        FirebaseEvents.firebaseUserAction(
                            "BeforeHome",
                            if(isHighFloor)"inter_bf_home_view_hf" else "inter_bf_home_view")
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
                ad.show(activity)
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