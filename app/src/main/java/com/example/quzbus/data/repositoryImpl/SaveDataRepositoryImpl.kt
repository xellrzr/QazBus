package com.example.quzbus.data.repositoryImpl

import com.example.quzbus.data.sharedpref.AppSharedPreferences
import com.example.quzbus.domain.repository.SaveDataRepository
import javax.inject.Inject

class SaveDataRepositoryImpl @Inject constructor(
    private val pref: AppSharedPreferences
) : SaveDataRepository {

    override fun setAccessToken(accessToken: String) {
        pref.setAccessToken(accessToken)
    }

    override fun setSelectCity(city: String) {
        pref.setSelectCity(city)
    }

    override fun setPhoneNumber(phoneNumber: String) {
        pref.setPhoneNumber(phoneNumber)
    }


}