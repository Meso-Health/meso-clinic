package org.watsi.device.managers

import android.content.Context
import android.net.ConnectivityManager

class NetworkManagerImpl(val context: Context) : NetworkManager {

    override fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}
