package com.example.quzbus.domain.repository

import com.example.quzbus.data.models.response.singleroute.BusRoute
import com.example.quzbus.utils.NetworkResult

interface SingleRouteRepository {

    suspend fun getSingleRoute(route: String): NetworkResult<BusRoute>
}