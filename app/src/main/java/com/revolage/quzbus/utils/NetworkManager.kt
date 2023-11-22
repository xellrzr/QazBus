package com.revolage.quzbus.utils

import android.net.ConnectivityManager
import android.net.Network

class NetworkManager(
    private val onNetworkAvailable: () -> Unit,
    private val onNetworkLost: () -> Unit
) : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onNetworkAvailable()
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        onNetworkLost()
    }
}