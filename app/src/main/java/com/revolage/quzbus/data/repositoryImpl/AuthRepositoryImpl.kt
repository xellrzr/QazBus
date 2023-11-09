package com.revolage.quzbus.data.repositoryImpl

import com.revolage.quzbus.data.models.response.Message
import com.revolage.quzbus.data.remote.QazBusApi
import com.revolage.quzbus.data.sharedpref.AppSharedPreferences
import com.revolage.quzbus.domain.repository.AuthRepository
import com.revolage.quzbus.utils.NetworkResult
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: QazBusApi,
    private val pref: AppSharedPreferences
) : AuthRepository, BaseRepository() {

    override suspend fun getSmsCode(phoneNumber: String): NetworkResult<Message> {
        return safeApiCall { api.getSmsCode(phoneNumber) }
    }

    override suspend fun getAuth(
        phoneNumber: String,
        password: String
    ): NetworkResult<Message> {
        pref.setPhoneNumber(phoneNumber)
        val result =  safeApiCall { api.getAuth(phoneNumber,password) }
        return if (result.data?.result?.length!! > 1) {
            pref.setAccessToken(result.data.result)
            result
        } else {
            result
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return pref.getAccessToken() != null
    }

}