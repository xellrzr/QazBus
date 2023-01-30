package com.example.quzbus.data.remote

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface QazBusApi {

    @POST("usersms")
    suspend fun getSmsCode(
        @Query("phone") phone: String
    ): Response<String>

}