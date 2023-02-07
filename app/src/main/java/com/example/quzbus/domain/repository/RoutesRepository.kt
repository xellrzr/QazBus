package com.example.quzbus.domain.repository

import com.example.quzbus.data.models.response.getbuses.GetBusesResponse
import com.example.quzbus.data.models.response.getroute.GetRouteResponse
import com.example.quzbus.domain.models.routes.Routes
import com.example.quzbus.utils.NetworkResult

interface RoutesRepository {

    suspend fun getRoutes(cityId: Int): NetworkResult<Routes>

    suspend fun getRoute(route: String): NetworkResult<GetRouteResponse>

    suspend fun getBuses(route: String): NetworkResult<GetBusesResponse>

}