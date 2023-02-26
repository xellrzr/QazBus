package com.example.quzbus.domain.repository

import com.example.quzbus.data.models.response.Message
import com.example.quzbus.utils.NetworkResult

interface CitiesRepository{

    suspend fun getCities(): NetworkResult<Message>

    fun setSelectCity(city: String?)

    fun setCityId(cityId: Int)

    fun isCitySelected(): Boolean

    fun getCityId(): Int

    fun getCity(): String?
}