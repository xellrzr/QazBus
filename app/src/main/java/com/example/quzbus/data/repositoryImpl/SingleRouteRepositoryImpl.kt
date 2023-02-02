package com.example.quzbus.data.repositoryImpl

import com.example.quzbus.data.models.response.singleroute.BusRoute
import com.example.quzbus.data.remote.QazBusApi
import com.example.quzbus.data.sharedpref.AppSharedPreferences
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
            return safeApiCall { api.getRoute(route, accessToken, phoneNumber, cityId) }
        }
        return NetworkResult.Error("Can't get Bus route")
    }
}