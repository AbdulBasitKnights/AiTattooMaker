package com.basit.aitattoomaker.presentation.splash.onboarding.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.FragmentSecondOnboardingBinding
import com.basit.aitattoomaker.presentation.MainActivity
import com.basit.aitattoomaker.presentation.utils.SharedPref


class OnboardingSecondFragment : Fragment() {
    var preferenceManager: SharedPreferences? = null
    private var binding: FragmentSecondOnboardingBinding? = null
    private var mActivity: FragmentActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }
    override fun onStart() {
        super.onStart()
        initializeVideoView()
    }
    private val nav: OnBoardingFragment.PagerNav by lazy {
        (parentFragment as? OnBoardingFragment.PagerNav)
            ?: (activity as? OnBoardingFragment.PagerNav)
            ?: error(
                "Host must implement OnboardingFragment.PagerNav " +
                        "(either the parent fragment or the activity)."
            )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSecondOnboardingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let {
            preferenceManager = SharedPref(it).getSharedPreferences()
            binding?.next?.setOnClickListener {
                preferenceManager?.edit()?.putBoolean("firstTime", false)?.apply()
                startActivity(Intent(mActivity, MainActivity::class.java))
                mActivity?.finish()
            }
        }

//        binding?.shimmerViewContainer?.startShimmer()
    }
    private fun initializeVideoView() {
        val videoUri = Uri.parse("android.resource://${requireContext().packageName}/${R.raw.ob2_video}")
        binding?.videoView?.setVideoURI(videoUri)

        // Start playing the video automatically
        binding?.videoView?.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true  // Optional: loop the video
            binding?.videoView?.start()             // Auto-play the video
        }
    }

}