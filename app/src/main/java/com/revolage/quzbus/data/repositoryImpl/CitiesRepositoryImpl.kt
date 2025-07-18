package com.revolage.quzbus.data.repositoryImpl

import com.revolage.quzbus.data.models.response.Message
import com.revolage.quzbus.data.remote.QazBusApi
import com.revolage.quzbus.data.sharedpref.AppSharedPreferences
import com.revolage.quzbus.domain.repository.CitiesRepository
import com.revolage.quzbus.utils.NetworkResult
import javax.inject.Inject

class CitiesRepositoryImpl @Inject constructor(
    private val api: QazBusApi,
    private val pref: AppSharedPreferences
) : CitiesRepository, BaseRepository() {

    override suspend fun getCities(): NetworkResult<Message> {
       return safeApiCall { api.getCities() }
    }

    override fun setSelectCity(city: String?) {
        pref.setSelectCity(city)
    }

    override fun setCityId(cityId: Int) {
        pref.setCityId(cityId)
    }

    override fun isCitySelected(): Boolean {
        return (pref.getSelectCity() != null)
    }

    override fun getCityId(): Int {
        return pref.getCityId()
    }

    override fun getCity(): String? {
        return pref.getSelectCity()
    }

}