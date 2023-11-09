package com.example.quzbus.domain.repository

import com.example.quzbus.data.models.response.Message
import com.example.quzbus.utils.NetworkResult

interface AuthRepository {

    suspend fun getSmsCode(phoneNumber: String): NetworkResult<Message>

    suspend fun getAuth(
        phoneNumber: String,
        password: String): NetworkResult<Message>

    fun isUserLoggedIn(): Boolean
}