package com.basit.aitattoomaker.presentation.application

import android.annotation.SuppressLint
import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import com.basit.aitattoomaker.ads.AppOpenManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.MobileAds


@HiltAndroidApp
class AppController : MultiDexApplication(),LifecycleObserver, DefaultLifecycleObserver {
    @SuppressLint("StaticFieldLeak")
    var appOpenManager: AppOpenManager? = null
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @SuppressLint("StaticFieldLeak")
    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        context =applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenManager =  AppOpenManager(this@AppController)
        MobileAds.initialize(applicationContext)
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)

    }
}
