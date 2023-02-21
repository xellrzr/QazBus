package com.example.quzbus.data.repositoryImpl

import android.util.Log
import com.example.quzbus.data.models.response.getbuses.GetBusesResponse
import com.example.quzbus.data.models.response.getroute.GetRouteResponse
import com.example.quzbus.data.remote.QazBusApi
import com.example.quzbus.data.sharedpref.AppSharedPreferences
import com.example.quzbus.domain.models.routes.Routes
import com.example.quzbus.domain.repository.RoutesRepository
import com.example.quzbus.utils.NetworkResult
import javax.inject.Inject

class RoutesRepositoryImpl @Inject constructor(
    private val api: QazBusApi,
    private val pref: AppSharedPreferences
) : RoutesRepository, BaseRepository() {

    private val accessToken = pref.getAccessToken()
    private val phoneNumber = pref.getPhoneNumber()
    private val cityId get() = pref.getCityId()

    override suspend fun getRoutes(): NetworkResult<Routes> {
        if (accessToken != null && phoneNumber != null && cityId != 0) {
            val response =  safeApiCall { api.getRoutes(accessToken, phoneNumber, cityId) }
            return when(response) {
                is NetworkResult.Success -> NetworkResult.Success(response.data!!.toRoutes())
                else -> NetworkResult.Loading()
            }
        }
        return NetworkResult.Error("Not Authorized")
    }

    override suspend fun getRoute(route: String): NetworkResult<GetRouteResponse> {
        if (accessToken != null && phoneNumber != null && cityId != 0) {
            val response =  safeApiCall { api.getRoute(route, accessToken, phoneNumber, cityId) }
            return when(response) {
                is NetworkResult.Success -> NetworkResult.Success(response.data!!)
                else -> NetworkResult.Loading()
            }
        }
        return NetworkResult.Error("Can't get Bus route")
    }

    override suspend fun getBuses(route: String): NetworkResult<GetBusesResponse> {
        if (accessToken != null && phoneNumber != null && cityId != 0) {
            val response = safeApiCall { api.getBuses(route,accessToken, phoneNumber, cityId) }
            return when(response) {
                is NetworkResult.Success -> NetworkResult.Success(response.data!!)
                else -> NetworkResult.Loading()
            }
        }
        return NetworkResult.Error("Can't get Buses routes")
    }

}