package com.revolage.quzbus.domain.repository

import com.revolage.quzbus.data.models.response.getbuses.GetBusesResponse
import com.revolage.quzbus.data.models.response.getroute.GetRouteResponse
import com.revolage.quzbus.domain.models.routes.Routes
import com.revolage.quzbus.utils.NetworkResult

interface RoutesRepository {

    suspend fun getRoutes(): NetworkResult<Routes>

    suspend fun getRoute(route: String): NetworkResult<GetRouteResponse>

    suspend fun getBuses(route: String): NetworkResult<GetBusesResponse>

}