package com.basit.aitattoomaker.presentation.splash.onboarding.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.data.repo.NetworkUtils
import com.basit.aitattoomaker.databinding.FragmentOnboardingfiveBinding
import com.basit.aitattoomaker.presentation.splash.onboarding.AdsManagerNew
import com.basit.aitattoomaker.presentation.splash.onboarding.AdsManagerNew.fo_enable_auto_next_full_scr
import com.basit.aitattoomaker.presentation.splash.onboarding.AdsManagerNew.fo_time_auto_next_full_scr
import com.basit.aitattoomaker.presentation.utils.FirebaseEvents
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OnboardingfiveFragment : Fragment() {

    private var binding : FragmentOnboardingfiveBinding? = null
    private var mActivity: FragmentActivity? = null
    var job : Job? = null

    private val nav: OnBoardingFragment.PagerNav by lazy {
        (parentFragment as? OnBoardingFragment.PagerNav)
            ?: (activity as? OnBoardingFragment.PagerNav)
            ?: error(
                "Host must implement OnboardingFragment.PagerNav " +
                        "(either the parent fragment or the activity)."
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentOnboardingfiveBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { loadNativeAd5(it)

            if(NetworkUtils.isOnline(it)) {
                FirebaseEvents.firebaseUserAction(
                    "",
                    ""
                )
            }}


    }


    override fun onResume() {
        super.onResume()
        if (fo_enable_auto_next_full_scr?:true) {
            job = lifecycleScope.launch {
                delay(fo_time_auto_next_full_scr)
                nav.goNext()
            }
        }
    }


    override fun onPause() {
        super.onPause()
        job?.cancel()
        job=null
    }



    fun loadNativeAd5(context: Context) {
        if (AdsManagerNew.ObnativeAdhigh5 == null && AdsManagerNew.fo_hf_native_full_scr2?:true) {
            try {
                val adLoader = AdLoader.Builder(context, "")
                    .forNativeAd { nativeAd ->
                        Log.w("checkNativeOB","Hf_Native_full_scr2: Loaded")
//                        FirebaseEvents.firebaseUserAction("fullscreenhigh1", "onboardingFragment")
                        AdsManagerNew.ObnativeAdhigh5 = nativeAd
                        Log.d("checkNativeOB","Hf_Native_full_scr2: Show")
                        loadNativeListTemplate5()
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("checkNativeOB","Hf_Native_full_scr2: Failed")
                            loadNativeAdOB5(context)
                        }
                    })
                    .build()
                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.d("checkNativeOB","Hf_Native_full_scr2: Show")
            loadNativeListTemplate5()
        }
    }

    private fun loadNativeListTemplate5() {
        try {
            val headlineView = binding?.primary5
            headlineView?.text = AdsManagerNew.ObnativeAdhigh5?.headline
            binding?.adBody5?.text = AdsManagerNew.ObnativeAdhigh5?.body
            val imageView = binding?.AdImage5
            imageView?.mediaContent = AdsManagerNew.ObnativeAdhigh5?.mediaContent
            val callToActionView = binding?.cta5
            callToActionView?.text = AdsManagerNew.ObnativeAdhigh5?.callToAction
            binding?.adViewLayout5?.headlineView = headlineView
            binding?.adViewLayout5?.mediaView = imageView
            binding?.adViewLayout5?.callToActionView = callToActionView
            AdsManagerNew.ObnativeAdhigh5?.let { binding?.adViewLayout5?.setNativeAd(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun loadNativeAdOB5(context: Context) {
        if (AdsManagerNew.ObnativeAd5 == null  && AdsManagerNew.native_full_sec2?:true) {
            try {
                val adLoader = AdLoader.Builder(context, "")
                    .forNativeAd { nativeAd ->
                        Log.w("checkNativeOB","Native_full_scr2: Loaded")
                        AdsManagerNew.ObnativeAd5 = nativeAd
//                        FirebaseEvents.firebaseUserAction("fullscreen1", "onboardingFragment")
                        Log.d("checkNativeOB","Native_full_scr2: Show")
                        loadNativeListTemplateNormalOB5()
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("checkNativeOB","Native_full_scr2: Failed")
                        }
                    }).build()
                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            loadNativeListTemplateNormalOB5()
        }
    }


    private fun loadNativeListTemplateNormalOB5() {
        try {
            val headlineView = binding?.primary5
            headlineView?.text = AdsManagerNew.ObnativeAd5?.headline
            binding?.adBody5?.text = AdsManagerNew.ObnativeAd5?.body
            val imageView = binding?.AdImage5
            imageView?.mediaContent = AdsManagerNew.ObnativeAd5?.mediaContent
            val callToActionView = binding?.cta5
            callToActionView?.text = AdsManagerNew.ObnativeAd5?.callToAction
            binding?.adViewLayout5?.headlineView = headlineView
            binding?.adViewLayout5?.mediaView = imageView
            binding?.adViewLayout5?.callToActionView = callToActionView
            AdsManagerNew.ObnativeAd5?.let { binding?.adViewLayout5?.setNativeAd(it) }
            binding?.adViewLayout5?.setOnClickListener {
                AdsManagerNew.ObnativeAd5?.performClick(Bundle())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}