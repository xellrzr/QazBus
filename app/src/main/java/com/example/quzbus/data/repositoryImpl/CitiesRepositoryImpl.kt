package com.example.quzbus.data.repositoryImpl

import com.example.quzbus.data.models.response.Message
import com.example.quzbus.data.remote.QazBusApi
import com.example.quzbus.domain.repository.CitiesRepository
import com.example.quzbus.utils.NetworkResult
import javax.inject.Inject

class CitiesRepositoryImpl @Inject constructor(
    private val api: QazBusApi
) : CitiesRepository, BaseRepository() {

    override suspend fun getCities(): NetworkResult<Message> {
       return safeApiCall { api.getCities() }
    }

}