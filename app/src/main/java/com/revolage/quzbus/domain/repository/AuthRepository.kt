package com.revolage.quzbus.domain.repository

import com.revolage.quzbus.data.models.response.Message
import com.revolage.quzbus.utils.NetworkResult

interface AuthRepository {

    suspend fun getSmsCode(phoneNumber: String): NetworkResult<Message>

    suspend fun getAuth(
        phoneNumber: String,
        password: String
    ): NetworkResult<Message>

    fun isUserLoggedIn(): Boolean
}