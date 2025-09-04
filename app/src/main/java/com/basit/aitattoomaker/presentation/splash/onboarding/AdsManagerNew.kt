package com.basit.aitattoomaker.presentation.splash.onboarding

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.basit.aitattoomaker.presentation.utils.FirebaseEvents
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd

@SuppressLint("StaticFieldLeak")
object AdsManagerNew {

    var ObnativeAd: NativeAd? = null
    var ObnativeAdhigh: NativeAd? = null
    var ObnativeAdhigh2: NativeAd? = null


    var ObnativeAd4: NativeAd? = null
    var ObnativeAdhigh4: NativeAd? = null

    var ObnativeAd5: NativeAd? = null
    var ObnativeAdhigh5: NativeAd? = null
    var native_ob2:Boolean?=false
    var hf_native_ob2:Boolean?=false
    var hf_native_ob3:Boolean?=false
    var fo_hf_native_full_scr1:Boolean?=false
    var fo_hf_native_full_scr2:Boolean?=false
    //Normal Full Screen Native Enable
    var native_full_sec1:Boolean?=false
    var native_full_sec2:Boolean?=false



    private val _slot4Loaded = MutableLiveData(false)
    val slot4Loaded: LiveData<Boolean> get() = _slot4Loaded

    private val _slot5Loaded = MutableLiveData(false)
    val slot5Loaded: LiveData<Boolean> get() = _slot5Loaded


    private fun postSlot4Ready() {
        if (_slot4Loaded.value != true) _slot4Loaded.postValue(true)
    }

    private fun postSlot5Ready() {
        if (_slot5Loaded.value != true) _slot5Loaded.postValue(true)
    }

    fun loadNativeHigh(context: Context){
        try {
            if(ObnativeAdhigh!=null || hf_native_ob2==false)
                return
            val adLoader = AdLoader.Builder(context, "")
                .forNativeAd { nativeAd ->
                    Log.d("nativeAd", "loaded")
                    ObnativeAdhigh = nativeAd
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {

                    }
                })
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadNativesimple(context: Context){
        try {
            if(ObnativeAd!=null || native_ob2==false)
                return
            val adLoader =
                AdLoader.Builder(context, "")
                    .forNativeAd { nativeAd ->
                        Log.d("nativeAd", "loaded")
                        ObnativeAd = nativeAd
                        FirebaseEvents.firebaseUserAction("native_ob2", "onboardingFragment")
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                        }
                    })
                    .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun loadNative2(context: Context){
        try {
            if(ObnativeAdhigh2!=null || hf_native_ob3==false)
                return
            val adLoader = AdLoader.Builder(context, "")
                .forNativeAd { nativeAd ->
                    Log.d("nativeAd", "loaded")
                    ObnativeAdhigh2 = nativeAd
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {

                    }
                })
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun loadNativeAd4(context: Context) {
        if (ObnativeAdhigh4 == null && fo_hf_native_full_scr1==true) {
            try {
                val adLoader = AdLoader.Builder(context, "")
                    .forNativeAd { nativeAd ->
                        ObnativeAdhigh4 = nativeAd
                        postSlot4Ready()
                        Log.w("checkNativeOB","Hf_Native_ob_full_Scr: Loaded")
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("checkNativeOB","Hf_Native_ob_full_Scr: Failed")
                        }
                    })
                    .build()
                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun loadNativeAdOB4(context: Context) {
        if (ObnativeAd4 == null && native_full_sec1==true) {
            try {
                val adLoader = AdLoader.Builder(context, "")
                    .forNativeAd { nativeAd ->
                        Log.w("checkNativeOB","Native_ob_full_Scr: Loaded")
                        ObnativeAd4 = nativeAd
                        postSlot4Ready()
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("checkNativeOB","Native_ob_full_Scr: Failed")
                        }
                    }).build()
                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun loadNativeAd5(context: Context) {
        if (ObnativeAdhigh5 == null && fo_hf_native_full_scr2==true) {
            try {
                val adLoader = AdLoader.Builder(context, "")
                    .forNativeAd { nativeAd ->
                        Log.d("nativeAd", "loaded")
                        ObnativeAdhigh5 = nativeAd
                        postSlot5Ready()
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                        }
                    })
                    .build()
                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun loadNativeAdOB5(context: Context) {
        if (ObnativeAd5 == null && native_full_sec2==true) {
            try {
                val adLoader = AdLoader.Builder(context, "")
                    .forNativeAd { nativeAd ->
                        Log.d("nativeAd", "loaded")
                        ObnativeAd5 = nativeAd
                        postSlot5Ready()
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.d("nativeAd", "not loaded")
                        }
                    }).build()
                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}