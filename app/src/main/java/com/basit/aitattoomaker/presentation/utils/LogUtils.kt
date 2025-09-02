package com.basit.aitattoomaker.presentation.utils

import android.util.Log

object LogUtils {
    private val TAG: String = "mTag"
    fun printErrorLog(message: String) {
        Log.e(TAG, "$message")
    }

    fun printDebugLog(message: String) {
        Log.d(TAG, "$message")
    }

    fun printInfoLog(message: String) {
        Log.i(TAG, "$message")
    }
}