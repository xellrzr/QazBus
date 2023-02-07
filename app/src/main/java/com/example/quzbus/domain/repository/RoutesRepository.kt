package com.example.quzbus.domain.repository

import com.example.quzbus.data.models.response.routesresponse.GetRoutesResponse
import com.example.quzbus.domain.models.routes.Routes
import com.example.quzbus.utils.NetworkResult

interface RoutesRepository {

    suspend fun getRoutes(
        cityId: Int
    ): NetworkResult<Routes>

}