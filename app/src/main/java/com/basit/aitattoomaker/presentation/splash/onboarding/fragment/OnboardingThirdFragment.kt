package com.basit.aitattoomaker.presentation.splash.onboarding.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentOnboardingBinding
import com.basit.aitattoomaker.presentation.MainActivity
import com.basit.aitattoomaker.presentation.splash.onboarding.AdsManagerNew
import com.basit.aitattoomaker.presentation.splash.onboarding.ObnativeAd3
import com.basit.aitattoomaker.presentation.splash.onboarding.ObnativeAdhigh3


class OnboardingThirdFragment : Fragment() {
    private var binding: FragmentOnboardingBinding? = null
    private var mActivity: FragmentActivity? = null
    var preferenceManager: SharedPreferences? = null

    private val nav: OnBoardingFragment.PagerNav by lazy {
        (parentFragment as? OnBoardingFragment.PagerNav)
            ?: (activity as? OnBoardingFragment.PagerNav)
            ?: error(
                "Host must implement OnboardingFragment.PagerNav " +
                        "(either the parent fragment or the activity)."
            )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        if (native_ob4?:true || hf_native_ob4?:true){
            loadNativeAd3()
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity->
            binding?.skip?.setOnClickListener {
                startActivity(Intent(activity, MainActivity::class.java))
                activity.finish()
            }
        }


//        binding?.shimmerViewContainer?.startShimmer()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    fun loadNativeAd3() {
        if (ObnativeAdhigh3 == null) {
            mActivity?.let {
                loadNativeOb3hf(it)
            }
        } else {
            Log.d("checkNativeOB","Hf_Native_ob3: Show")
            loadNativeListTemplate3()
        }
    }

    private fun loadNativeListTemplate3() {

    }

    fun loadNativeAd() {
        if (ObnativeAd3 == null ) {
            mActivity?.let {
                loadNativeOb3(it)
            }

        } else {
            if(ObnativeAdhigh3==null) {
                Log.d("checkNativeOB","Native_ob3: Show")
                loadNativeListTemplateNormalOB3()
            }
            else{
                loadNativeListTemplate3()
            }
        }
    }

    private fun loadNativeListTemplateNormalOB3() {

    }
    fun loadNativeOb3hf(context: Context){
    }

    fun loadNativeOb3(context: Context){
    }
}