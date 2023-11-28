package com.revolage.quzbus.data.repositoryImpl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.revolage.quzbus.domain.repository.NetworkConnectivityRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkConnectivityRepositoryImpl @Inject constructor(
    context: Context
) : NetworkConnectivityRepository {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    override fun observeNetworkConnection(): Flow<NetworkConnectivityRepository.Status> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { trySend(NetworkConnectivityRepository.Status.AVAILABLE) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch { trySend(NetworkConnectivityRepository.Status.LOSING) }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { trySend(NetworkConnectivityRepository.Status.LOST) }
                }

                override fun onUnavailable() {
                    launch { trySend(NetworkConnectivityRepository.Status.UNAVAILABLE) }
                    super.onUnavailable()
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

}