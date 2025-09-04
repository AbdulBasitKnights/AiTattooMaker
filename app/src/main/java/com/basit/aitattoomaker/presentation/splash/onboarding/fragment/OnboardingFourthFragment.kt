package com.basit.aitattoomaker.presentation.splash.onboarding.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.data.repo.NetworkUtils
import com.basit.aitattoomaker.databinding.FragmentOnboardingfiveBinding
import com.basit.aitattoomaker.presentation.splash.onboarding.AdsManagerNew
import com.basit.aitattoomaker.presentation.splash.onboarding.fo_enable_auto_next_full_scr
import com.basit.aitattoomaker.presentation.splash.onboarding.fo_time_auto_next_full_scr
import com.basit.aitattoomaker.presentation.utils.FirebaseEvents
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OnboardingFourthFragment : Fragment() {

    private var binding: FragmentOnboardingfiveBinding? = null
    var job : Job? = null

    private val nav: OnBoardingFragment.PagerNav by lazy {
        (parentFragment as? OnBoardingFragment.PagerNav)
            ?: (activity as? OnBoardingFragment.PagerNav)
            ?: error(
                "Host must implement OnboardingFragment.PagerNav " +
                        "(either the parent fragment or the activity)."
            )
    }


    private var mActivity: FragmentActivity? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnboardingfiveBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { loadNativeAd4(it)
            if(NetworkUtils.isOnline(it)) {
                FirebaseEvents.firebaseUserAction(
                    "",
                    ""
                )
            }}


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
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

    fun loadNativeAd4(context: Context) {
        if (ObnativeAdhigh4 == null && fo_hf_native_full_scr1?:true) {
            try {
                val adLoader = AdLoader.Builder(context, context.resources.getString(R.string.App_ID))
                    .forNativeAd { nativeAd ->
                        Log.w("checkNativeOB","Hf_Native_ob_full_Scr: Loaded")
                        ObnativeAdhigh4 = nativeAd
//                        FirebaseEvents.firebaseUserAction("fullscreenhigh", "onboardingFragment")
                        loadNativeListTemplate4()
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("checkNativeOB","Hf_Native_ob_full_Scr: Failed")
                            loadNativeAdOB4(context)
                        }
                    })
                    .build()
                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.d("checkNativeOB","Hf_Native_ob_full_Scr: Show")
            loadNativeListTemplate4()
        }
    }

    private fun loadNativeListTemplate4() {
        try {
            val headlineView = binding?.primary5
            headlineView?.text = ObnativeAdhigh4?.headline
            binding?.adBody5?.text = ObnativeAdhigh4?.body
            val imageView = binding?.AdImage5
            imageView?.mediaContent = ObnativeAdhigh4?.mediaContent
            val callToActionView = binding?.cta5
            callToActionView?.text = ObnativeAdhigh4?.callToAction
            binding?.adViewLayout5?.headlineView = headlineView
            binding?.adViewLayout5?.mediaView = imageView
            binding?.adViewLayout5?.callToActionView = callToActionView
            ObnativeAdhigh4?.let { binding?.adViewLayout5?.setNativeAd(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun loadNativeAdOB4(context: Context) {
        if (ObnativeAd4 == null && native_full_sec1?:true) {
            try {
                val adLoader = AdLoader.Builder(context, context.resources.getString(R.string.App_ID))
                    .forNativeAd { nativeAd ->
                        Log.w("checkNativeOB","Native_ob_full_Scr: Loaded")
                        ObnativeAd4 = nativeAd
//                        FirebaseEvents.firebaseUserAction("fullscreen", "onboardingFragment")
                        Log.d("checkNativeOB","Native_ob_full_Scr: Show")
                        loadNativeListTemplateNormalOB4()
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
        } else {
            loadNativeListTemplateNormalOB4()
        }
    }

    private fun loadNativeListTemplateNormalOB4() {
        try {
            val headlineView = binding?.primary5
            headlineView?.text = ObnativeAd4?.headline
            binding?.adBody5?.text = ObnativeAd4?.body
            val imageView = binding?.AdImage5
            imageView?.mediaContent = ObnativeAd4?.mediaContent
            val callToActionView = binding?.cta5
            callToActionView?.text = ObnativeAd4?.callToAction
            binding?.adViewLayout5?.headlineView = headlineView
            binding?.adViewLayout5?.mediaView = imageView
            binding?.adViewLayout5?.callToActionView = callToActionView
            ObnativeAd4?.let { binding?.adViewLayout5?.setNativeAd(it) }

            binding?.adViewLayout5?.setOnClickListener {
                ObnativeAd4?.performClick(Bundle())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}