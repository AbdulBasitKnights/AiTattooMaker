package com.basit.aitattoomaker.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.ads.AdsManager
import com.basit.aitattoomaker.data.repo.NetworkUtils
import com.basit.aitattoomaker.databinding.ActivitySplashBinding
import com.basit.aitattoomaker.presentation.MainActivity
import com.basit.aitattoomaker.presentation.splash.onboarding.OnBoardingActivity
import com.basit.aitattoomaker.presentation.utils.AppUtils.FIRST_TIME_KEY
import com.basit.aitattoomaker.presentation.utils.FirebaseEvents
import com.basit.aitattoomaker.presentation.utils.LogUtils
import com.basit.aitattoomaker.presentation.utils.SharedPref
import com.singular.sdk.Singular
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    companion object {
        private var isSingularInitialized = false
    }
    private lateinit var preferenceManager: android.content.SharedPreferences
    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    private val viewmodel: SplashviewModel by viewModels()
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager =
            SharedPref(this@SplashActivity)
                .getSharedPreferences()
        AdsManager.loadInterstitialAdSplash(this,resources.getString(R.string.inter_af_home_hf),resources.getString(R.string.inter_af_home),{
            navigate()
        },{
            navigate()
        },{
            navigate()
        })
        initSingularSdk()
    }
    private fun initSingularSdk() {
        if (!isSingularInitialized && NetworkUtils.isOnline(this)) {
            CoroutineScope(Dispatchers.IO).launch {
                viewmodel.singularConfig.withSingularLink(
                    intent
                ) { singularLinkParams ->
                    LogUtils.printDebugLog("DEEPLINK_KEY :" + singularLinkParams.deeplink)
                    LogUtils.printDebugLog("PASSTHROUGH_KEY :" + singularLinkParams.passthrough)
                    LogUtils.printDebugLog("IS_DEFERRED_KEY :" + singularLinkParams.isDeferred)
                }
                isSingularInitialized = Singular.init(this@SplashActivity, viewmodel.singularConfig)
            }
        }
        FirebaseEvents.firebaseUserAction("Splash","splash_view")
    }
    fun navigate(){
        lifecycleScope.launch {
            if(preferenceManager.getBoolean(FIRST_TIME_KEY, true)==true){
                startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                finish()
            }
            else{
                AdsManager.showInterstitialSplash(this@SplashActivity, AdsManager.inter_bf_home_hf?: AdsManager.inter_bf_home,if(AdsManager.inter_bf_home_hf!=null)true else false,{
                    navigateToMain()
                },{
                    navigateToMain()
                },{
                    navigateToMain()
                },{
                    navigateToMain()
                })
            }
        }
    }
    fun navigateToMain(){
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }
    override fun onResume() {
        super.onResume()
    }
    override fun onPause() {
        super.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}