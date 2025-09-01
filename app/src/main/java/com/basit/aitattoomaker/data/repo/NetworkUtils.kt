package com.basit.aitattoomaker.data.repo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkUtils {
    companion object {
        fun isOnline(context: Context): Boolean {
            try {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                if (connectivityManager != null) {
                    val network = connectivityManager.activeNetwork
                    if (network != null) {
                        val nc = connectivityManager.getNetworkCapabilities(network)
                        return nc?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true ||
                                nc?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    }
                }
            } catch (e: NullPointerException) {
                // Handle the NullPointerException gracefully
                e.printStackTrace()
            }
            return false
        }

    }
}