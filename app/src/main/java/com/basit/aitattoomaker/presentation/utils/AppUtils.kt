package com.basit.aitattoomaker.presentation.utils

import androidx.fragment.app.FragmentActivity
import com.basit.aitattoomaker.MainActivity

object AppUtils {
    fun getMain(activity: FragmentActivity?): MainActivity? {
        return activity as? MainActivity
    }

}