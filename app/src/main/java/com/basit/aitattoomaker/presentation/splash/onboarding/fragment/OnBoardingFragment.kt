package com.basit.aitattoomaker.presentation.splash.onboarding.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.databinding.FragmentOnboardingBinding
import com.basit.aitattoomaker.presentation.MainActivity
import com.basit.aitattoomaker.presentation.splash.onboarding.OnBoardingActivity
import kotlinx.coroutines.Job

class OnBoardingFragment : Fragment() {

    private var binding: FragmentOnboardingBinding? = null
    var currentVideoIndex = 0
    var secondAdSelection:Boolean=false
    private var mActivity: FragmentActivity? = null
    var job: Job? = null
    private var pageIndex = 0
    private var pageCount = 1          // receive from host

    companion object {
        fun newInstance(@LayoutRes layout: Int, index: Int) = OnBoardingFragment().apply {
            arguments = bundleOf("layout" to layout, "index" to index)
        }
    }

    interface PagerNav {
        fun goNext()
        fun pageCount(): Int
    }

    private val nav: PagerNav by lazy {
        // parentFragment first → works if pager is inside a fragment
        (parentFragment as? PagerNav) ?: (activity as PagerNav)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageIndex = arguments?.getInt("index") ?: 0
        pageCount = nav.pageCount()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity()
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentOnboardingBinding.inflate(inflater, container, false)

        mActivity?.let {activity->
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity?.let { activity->
            binding?.skip?.setOnClickListener {
                mActivity?.startActivity(Intent(activity, MainActivity::class.java))
                mActivity?.finish()
            }
            binding?.next?.setOnClickListener {
                nav.goNext()
            }
        }

    }

    override fun onResume() {
        super.onResume()
//        val host = activity as OnBoardingActivity
//        host.diableSwipe()
    }
}

