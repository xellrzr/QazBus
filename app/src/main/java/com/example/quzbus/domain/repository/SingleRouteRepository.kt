package com.example.quzbus.domain.repository

import com.example.quzbus.data.models.response.singlerouteresponse.GetBusRouteResponse
import com.example.quzbus.domain.models.buses.RouteBuses
import com.example.quzbus.domain.models.busroute.BusRoute
import com.example.quzbus.utils.NetworkResult

interface SingleRouteRepository {

    suspend fun getSingleRoute(route: String): NetworkResult<BusRoute>

    suspend fun getBusesRoutes(route: String): NetworkResult<RouteBuses>
}