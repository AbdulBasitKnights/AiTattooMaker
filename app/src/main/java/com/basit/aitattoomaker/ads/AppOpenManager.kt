package com.basit.aitattoomaker.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.ads.AdsManager.isPremiumSubscription
import com.basit.aitattoomaker.ads.AdsManager.isShowingAd
import com.basit.aitattoomaker.presentation.application.AppController
import com.basit.aitattoomaker.presentation.application.AppController.Companion.context
import com.basit.aitattoomaker.presentation.splash.onboarding.AdsManagerNew
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Prefetches App Open Ads.
 */
class AppOpenManager(private val myApplication: AppController) : LifecycleObserver,
    Application.ActivityLifecycleCallbacks {
    var appOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null

    init {
        myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * LifecycleObserver methods
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
//        if (currentActivity !is SplashActivity)
        showAdIfAvailable()
    }
    fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable && isPremiumSubscription.value != true && !isSplash) {
//            if (GlobalValues.is24hourEnabled.value == false) {


                val fullScreenContentCallback: FullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null
                            isShowingAd = false
                            //                            MainActivity.Companion.setShowing(false);
//                            if (MainActivity.Companion.isShowing()) {
                            fetchAd()
                            //                            }
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {

                        }

                        override fun onAdShowedFullScreenContent() {
                            isShowingAd = true
                        }
                    }
                appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
                currentActivity?.let {
                    appOpenAd?.show(it)
                    isShowingAd = false}
        } else {
                fetchAd()
        }
    }

    /**
     * Request an ad
     */
    fun fetchAd() {
        if (isAdAvailable) {
            /*if (currentActivity is SplashActivity) {
                openAdLoaded.value = 1
            }*/
            return
        }
        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                loadTime = Date().time
            }


            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
                Log.e("ERROR", "onAdFailedToLoad")
            }

        }
        val request = adRequest
        if (isPremiumSubscription.value == false) {
                if (AD_UNIT_ID_HF != null) {
                    loadCallback?.let {
                        CoroutineScope(Dispatchers.Main).launch {
                            AppOpenAd.load(
                                myApplication,
                                AD_UNIT_ID_HF,
                                request,
                                it
                            )
                        }
                    }
                }
        }
    }

    /**
     * Creates and returns ad request.
     */
    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    private val isAdAvailable: Boolean
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    companion object {
        private val AD_UNIT_ID = context?.getString(R.string.App_Open_Ad)
        private val AD_UNIT_ID_HF = context?.getString(R.string.App_Open_Ad_hf)
        var isSplash = true
    }
}