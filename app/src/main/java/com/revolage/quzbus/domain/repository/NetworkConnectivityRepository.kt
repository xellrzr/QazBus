package com.revolage.quzbus.domain.repository

import kotlinx.coroutines.flow.Flow


interface NetworkConnectivityRepository {

    fun observeNetworkConnection(): Flow<Status>

    enum class Status {
        UNAVAILABLE,
        AVAILABLE,
        LOSING,
        LOST
    }
}

