package com.basit.aitattoomaker.presentation.splash.onboarding.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.data.repo.NetworkUtils
import com.basit.aitattoomaker.databinding.FragmentOnboardingfiveBinding
import com.basit.aitattoomaker.presentation.utils.FirebaseEvents
import kotlinx.coroutines.Job

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
        mActivity?.let { }


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

    }


    override fun onPause() {
        super.onPause()
        job?.cancel()
        job=null
    }



}