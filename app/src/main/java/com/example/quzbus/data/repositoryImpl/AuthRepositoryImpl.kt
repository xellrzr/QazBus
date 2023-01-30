package com.example.quzbus.data.repositoryImpl

import com.example.quzbus.data.models.response.Message
import com.example.quzbus.data.remote.QazBusApi
import com.example.quzbus.domain.repository.AuthRepository
import com.example.quzbus.utils.NetworkResult
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: QazBusApi
) : AuthRepository, BaseRepository() {

    override suspend fun getSmsCode(phoneNumber: String): NetworkResult<Message> {
        return safeApiCall { api.getSmsCode(phoneNumber) }
    }

    override suspend fun getAuth(
        phoneNumber: String,
        language: String,
        password: String
    ): NetworkResult<Message> {
        return safeApiCall { api.getAuth(phoneNumber,language,password) }
    }

}