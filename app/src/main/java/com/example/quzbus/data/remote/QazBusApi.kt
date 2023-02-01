package com.example.quzbus.data.remote

import com.example.quzbus.data.models.response.Message
import com.example.quzbus.data.models.response.Routes
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface QazBusApi {

    @FormUrlEncoded
    @POST("useroperator")
    suspend fun getCities(
        @Field("phone") phone: String = "77770000000"
    ): Response<Message>

    @POST("usersms")
    suspend fun getSmsCode(
        @Query("phone") phone: String
    ): Response<Message>

    @FormUrlEncoded
    @POST("useroperator")
    suspend fun getAuth(
        @Field("phone") phone: String,
        @Field("lang") lang: String = "0",
        @Field("pas") pas: String
    ): Response<Message>

    @FormUrlEncoded
    @POST("userauto")
    suspend fun getRoutes(
        @Field("sid") sid: String,
        @Field("phone") phone: String,
        @Field("city") city: Int,
        @Field("id") id: Int = 0,//0
        @Field("route") route: Int = 0//0
    ): Response<Routes>
}