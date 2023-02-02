package com.example.quzbus.data.repositoryImpl

import android.util.Log
import com.example.quzbus.data.models.response.Routes
import com.example.quzbus.data.remote.QazBusApi
import com.example.quzbus.data.sharedpref.AppSharedPreferences
import com.example.quzbus.domain.repository.RoutesRepository
import com.example.quzbus.utils.DataResult
import com.example.quzbus.utils.NetworkResult
import javax.inject.Inject

class RoutesRepositoryImpl @Inject constructor(
    private val api: QazBusApi,
    private val pref: AppSharedPreferences
) : RoutesRepository, BaseRepository() {

    private val accessToken: String? = pref.getAccessToken()
    private val phoneNumber: String? = pref.getPhoneNumber()

    override suspend fun getRoutes(cityId: Int): NetworkResult<Routes> {
        if (accessToken != null && phoneNumber != null) {
            pref.setCityId(cityId)
            Log.d("TAG", "SUCCESS")
            Log.d("TAG", "accessToken = $accessToken, phoneNumber = $phoneNumber")
            return safeApiCall { api.getRoutes(accessToken, phoneNumber, cityId) }
//            return networkResult.result(networkResult)
        }
        Log.d("TAG", "ERROR")
        Log.d("TAG", "accessToken = $accessToken, phoneNumber = $phoneNumber")
        return NetworkResult.Error("Not Authorized")
    }

}