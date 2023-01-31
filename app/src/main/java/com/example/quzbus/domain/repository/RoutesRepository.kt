package com.example.quzbus.domain.repository

import com.example.quzbus.data.models.response.Routes
import com.example.quzbus.utils.NetworkResult

interface RoutesRepository {

    suspend fun getRoutes(
        accessToken: String,
        phoneNumber: String,
        city: String,
        id: Int,
        route: Int
    ): NetworkResult<Routes>
}