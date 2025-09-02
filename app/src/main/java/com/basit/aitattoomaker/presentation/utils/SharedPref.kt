package com.basit.aitattoomaker.presentation.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPref(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TattooPrefs", Context.MODE_PRIVATE)

    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences
    }

    var isFirstTime: Boolean
        get() = sharedPreferences.getBoolean("firstTime", true)
        set(value) {
            sharedPreferences.edit().putBoolean("firstTime", value).apply()
        }
}