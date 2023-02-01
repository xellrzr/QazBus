package com.example.quzbus.domain.repository

import com.example.quzbus.data.models.response.Routes
import com.example.quzbus.utils.DataResult
import com.example.quzbus.utils.NetworkResult

interface RoutesRepository {

    suspend fun getRoutes(
        cityId: Int
    ): NetworkResult<Routes>

}