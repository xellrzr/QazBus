package com.example.quzbus.domain.repository

import com.example.quzbus.utils.NetworkResult

interface AuthRepository {

    suspend fun getSmsCode(phoneNumber: String): NetworkResult<String>
}