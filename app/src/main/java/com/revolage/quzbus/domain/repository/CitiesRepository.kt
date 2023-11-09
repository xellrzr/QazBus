package com.revolage.quzbus.domain.repository

import com.revolage.quzbus.data.models.response.Message
import com.revolage.quzbus.utils.NetworkResult

interface CitiesRepository{

    suspend fun getCities(): NetworkResult<Message>

    fun setSelectCity(city: String?)

    fun setCityId(cityId: Int)

    fun isCitySelected(): Boolean

    fun getCityId(): Int

    fun getCity(): String?
}