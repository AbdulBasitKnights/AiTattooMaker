package com.basit.aitattoomaker.presentation.application

import android.annotation.SuppressLint
import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver

class AppController : MultiDexApplication(),LifecycleObserver, DefaultLifecycleObserver {


    @SuppressLint("StaticFieldLeak")
    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        context =this@AppController
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)

    }
}
