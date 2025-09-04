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
        mActivity?.let {
        }

    }


    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
        job?.cancel()
        job=null
    }




}