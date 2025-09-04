package com.basit.aitattoomaker.presentation.splash.onboarding.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.basit.aitattoomaker.presentation.splash.onboarding.AdsManagerNew
import com.basit.aitattoomaker.presentation.splash.onboarding.fragment.OnBoardingFragment
import com.basit.aitattoomaker.presentation.splash.onboarding.fragment.OnboardingFourthFragment
import com.basit.aitattoomaker.presentation.splash.onboarding.fragment.OnboardingSecondFragment
import com.basit.aitattoomaker.presentation.splash.onboarding.fragment.OnboardingThirdFragment
import com.basit.aitattoomaker.presentation.splash.onboarding.fragment.OnboardingfiveFragment
import kotlin.reflect.KClass

class OnboardingViewPager(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    // ---------- internal page enum -----------------------------------------------------------

    private enum class Page(
        val clazz: KClass<out Fragment>,
        val id: Long,
        val factory: () -> Fragment
    ) {
        CORE_1(OnBoardingFragment::class,        11L, { OnBoardingFragment() }),
        AD_4 (OnboardingFourthFragment::class,   12L, { OnboardingFourthFragment() }),
        CORE_3(OnboardingSecondFragment::class,   13L, { OnboardingSecondFragment() }),
        AD_5 (OnboardingfiveFragment::class,     14L, { OnboardingfiveFragment() }),
        CORE_4(OnboardingThirdFragment::class,   15L, { OnboardingThirdFragment() }),
    }

    // ---------- mutable list of *current* pages ----------------------------------------------

    private val pages = mutableListOf<Page>()

    init {
        rebuildPages()
    }

    /** Call from ad‑load callbacks to insert/remove ad pages and refresh the ViewPager */
    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        val before = pages.toList()
        rebuildPages()
        // Quick & safe: redraw everything (fine for <10 pages)
        if (before != pages) notifyDataSetChanged()
        // For perfectionists: diff `before` vs `pages` and call notifyItemInserted/Removed.
    }

    private fun rebuildPages() {
        pages.clear()

        pages += Page.CORE_1
        if (ObnativeAdhigh4 != null || ObnativeAd4 != null) {
            pages += Page.AD_4
        }
        pages += Page.CORE_3
        if (ObnativeAdhigh5 != null || ObnativeAd5 != null) {
            pages += Page.AD_5
        }
        pages += Page.CORE_4
    }

    // ---------- FragmentStateAdapter overrides -----------------------------------------------

    override fun getItemCount(): Int                 = pages.size
    override fun createFragment(position: Int): Fragment = pages[position].factory()
    override fun getItemId(position: Int): Long      = pages[position].id
    override fun containsItem(itemId: Long): Boolean = pages.any { it.id == itemId }
}
