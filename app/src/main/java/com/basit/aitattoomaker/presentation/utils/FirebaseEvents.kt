package com.basit.aitattoomaker.presentation.utils

import android.content.Context
import android.os.Bundle
import com.basit.aitattoomaker.presentation.application.AppController.Companion.context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.singular.sdk.Singular
import com.singular.sdk.SingularAdData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirebaseEvents {
    var firebaseAnalytics: FirebaseAnalytics? = null
    fun firebaseUserAction(activityName: String, actionName: String) {
        val action=formatString(actionName)
      /*  CoroutineScope(Dispatchers.IO).launch {
            context?.let {
                if (FirebaseApp.getApps(it).isEmpty()) {
                    FirebaseApp.initializeApp(it)
                } else {
                    if (firebaseAnalytics == null) {
                        firebaseAnalytics = Firebase.analytics
                    }
                    firebaseAnalytics?.let { analytics ->
                        analytics.logEvent(action) {
                            param("Screen_Name", activityName)
                        }
                    }
                    Singular.event(action)
                }
            }
        }*/
    }


    fun formatString(input: String): String {
        try {
            return input
                .replace(" ", "_")                  // Replace spaces with underscores
                .take(40)
        } catch (e: Exception) {
            return input
        }                           // Trim to max 40 characters
    }
    fun extractMediatedNetworkName(className: String?): String {
        return className
            ?.substringAfter("mediation.")       // "applovin.AppLovinMediationAdapter"
            ?.substringBefore('.')               // "applovin"
            ?.lowercase() ?: "unknown"
    }

}