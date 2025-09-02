package com.basit.aitattoomaker.presentation.splash.onboarding

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.ActivityMainBinding
import com.basit.aitattoomaker.databinding.ActivityOnboardingBinding
import com.basit.aitattoomaker.extension.hideSystemBars
import com.basit.aitattoomaker.presentation.MainViewModel
import com.basit.aitattoomaker.presentation.splash.onboarding.adapter.OnboardingViewPager
import com.basit.aitattoomaker.presentation.splash.onboarding.fragment.OnBoardingFragment
import kotlin.getValue

class OnBoardingActivity : FragmentActivity(), OnBoardingFragment.PagerNav {
    var viewPager: ViewPager2?=null
    val binding: ActivityOnboardingBinding by lazy {
        ActivityOnboardingBinding.inflate(layoutInflater)
    }
    private val pagerAdapter by lazy { OnboardingViewPager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        this.hideSystemBars()
        viewPager= findViewById(R.id.viewPager2)
        viewPager?.adapter = OnboardingViewPager(this)
        viewPager?.isUserInputEnabled = false
//        viewPager?.controlSwipe(
//            blockSwipeBackOnPages = setOf(1),
//            blockSwipeForwardOnPages = setOf(5)
//        )

//        isSplash =true
        if (AdsManagerNew.native_full_sec1?:true){
            AdsManagerNew.loadNativeAdOB4(this)
        }

        if (AdsManagerNew.fo_hf_native_full_scr1?:true) {
            AdsManagerNew.loadNativeAd4(this)
        }

        if (AdsManagerNew.native_full_sec2?:true){
            AdsManagerNew.loadNativeAdOB5(this)
        }

        if (AdsManagerNew.fo_hf_native_full_scr2?:true) {
            AdsManagerNew.loadNativeAd5(this)
        }

        setViewPagerAdapter()
    }
    fun shouldBlockForwardSwipe(): Boolean {
        // Replace with your condition
        return false
    }

    fun shouldBlockBackSwipe(): Boolean {
        // Replace with your condition
        return true
    }
//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            hideNavigationBar()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        this.hideNavigationBar()
//    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setViewPagerAdapter() {
        viewPager= findViewById(R.id.viewPager2)
        viewPager?.adapter = pagerAdapter
        AdsManagerNew.slot4Loaded.observe(this) { if (it) pagerAdapter.refresh() }
        AdsManagerNew.slot5Loaded.observe(this) { if (it) pagerAdapter.refresh() }
    }

    fun goNextPage() {
        val vp = binding.viewPager2
        val next = vp.currentItem + 1
        if (next < pagerAdapter.itemCount) {
            vp.setCurrentItem(next, true)   // smooth scroll
        }
        viewPager?.isUserInputEnabled = true
    }
    fun diableSwipe() {
        viewPager?.isUserInputEnabled = false
    }

    override fun goNext() {
        val next = binding.viewPager2.currentItem + 1
        if (next < pagerAdapter.itemCount) {
            binding.viewPager2.setCurrentItem(next, true)
        }
    }

    override fun pageCount(): Int = pagerAdapter.itemCount
    @SuppressLint("ClickableViewAccessibility")
    fun ViewPager2.disableSwipeBackOnPage(targetPage: Int) {
        val recyclerView = this.getChildAt(0) as? RecyclerView ?: return

        var initialX = 0f
        recyclerView.setOnTouchListener { _, event ->
            if (currentItem == targetPage) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = event.x
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.x - initialX
                        if (deltaX > 0) {
                            // Swiping backward
                            return@setOnTouchListener true // Block the touch
                        }
                    }
                }
            }
            false // Allow all other interactions
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    fun ViewPager2.disableSwipeForwardOnPage(targetPage: Int) {
        val recyclerView = this.getChildAt(0) as? RecyclerView ?: return

        var initialX = 0f
        recyclerView.setOnTouchListener { _, event ->
            if (currentItem == targetPage) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = event.x
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.x - initialX
                        if (deltaX < 0) {
                            // Swiping forward
                            return@setOnTouchListener true // Block the swipe
                        }
                    }
                }
            }
            false // Allow other interactions
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    fun ViewPager2.controlSwipe(
        blockSwipeBackOnPages: Set<Int> = emptySet(),
        blockSwipeForwardOnPages: Set<Int> = emptySet()
    ) {
        val recyclerView = this.getChildAt(0) as? RecyclerView ?: return
        var initialX = 0f

        recyclerView.setOnTouchListener { _, event ->
            val currentPage = this.currentItem

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.x
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - initialX

                    // Swiping backward (to previous page)
                    if (deltaX > 0 && currentPage in blockSwipeBackOnPages) {
                        return@setOnTouchListener true
                    }

                    // Swiping forward (to next page)
                    if (deltaX < 0 && currentPage in blockSwipeForwardOnPages) {
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

}