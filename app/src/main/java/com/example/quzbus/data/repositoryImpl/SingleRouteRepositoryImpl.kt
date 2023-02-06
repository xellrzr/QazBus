package com.example.quzbus.data.repositoryImpl

import com.example.quzbus.data.models.response.singlerouteresponse.GetBusRouteResponse
import com.example.quzbus.data.remote.QazBusApi
import com.example.quzbus.data.sharedpref.AppSharedPreferences
import com.example.quzbus.domain.models.busroute.BusRoute
import com.example.quzbus.domain.repository.SingleRouteRepository
import com.example.quzbus.utils.NetworkResult
import javax.inject.Inject

class SingleRouteRepositoryImpl @Inject constructor(
    private val api: QazBusApi,
    private val pref: AppSharedPreferences
) : SingleRouteRepository, BaseRepository() {

    private val accessToken: String? = pref.getAccessToken()
    private val phoneNumber: String? = pref.getPhoneNumber()
    private val cityId: Int = pref.getCityId()

    override suspend fun getSingleRoute(route: String): NetworkResult<BusRoute> {
        if (accessToken != null && phoneNumber != null && cityId != 0) {
            val response =  safeApiCall { api.getBusRoute(route, accessToken, phoneNumber, cityId) }
            return when(response) {
                is NetworkResult.Success -> NetworkResult.Success(response.data!!.toBusRoute())
                else -> NetworkResult.Loading()
            }
        }
        return NetworkResult.Error("Can't get Bus route")
    }
}