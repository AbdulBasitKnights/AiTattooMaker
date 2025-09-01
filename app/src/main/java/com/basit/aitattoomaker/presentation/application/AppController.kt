package com.basit.aitattoomaker.presentation.application

import android.annotation.SuppressLint
import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AppController : MultiDexApplication(),LifecycleObserver, DefaultLifecycleObserver {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @SuppressLint("StaticFieldLeak")
    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        context =applicationContext
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)

    }
}
