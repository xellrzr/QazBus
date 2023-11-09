package com.example.quzbus.data.remote

import com.example.quzbus.data.models.response.getroute.GetRouteResponse
import com.example.quzbus.data.models.response.Message
import com.example.quzbus.data.models.response.getroutes.GetRoutesResponse
import com.example.quzbus.data.models.response.getbuses.GetBusesResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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
        @Field("pas") pas: String,
        @Field("lang") lang: String = "0"
    ): Response<Message>

    //Метод на получение списка маршрутов для города
    @FormUrlEncoded
    @POST("userauto")
    suspend fun getRoutes(
        @Field("sid") sid: String,
        @Field("phone") phone: String,
        @Field("city") city: Int,
        @Field("id") id: Int = 0,//0
        @Field("route") route: Int = 0//0
    ): Response<GetRoutesResponse>

    //Метод на получение маршрута
    @FormUrlEncoded
    @POST("userroute")
    suspend fun getRoute(
        @Field("route") route: String,
        @Field("sid") sid: String,
        @Field("phone") phone: String,
        @Field("city") city: Int,
        @Field("id") id: Int = 1
    ): Response<GetRouteResponse>

    //Метод на получение списка автобусов для выбранного маршрута
    @FormUrlEncoded
    @POST("userlocation")
    suspend fun getBuses(
        @Field("route") route: String,
        @Field("sid") sid: String,
        @Field("phone") phone: String,
        @Field("city") city: Int,
        @Field("id") id: Int = 1
    ): Response<GetBusesResponse>
}