package com.basit.aitattoomaker.presentation.splash.onboarding.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.databinding.FragmentOnboardingBinding
import com.basit.aitattoomaker.presentation.MainActivity


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
}