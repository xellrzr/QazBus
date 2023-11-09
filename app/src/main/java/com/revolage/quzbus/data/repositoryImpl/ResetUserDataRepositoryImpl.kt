package com.example.quzbus.data.repositoryImpl

import com.example.quzbus.data.sharedpref.AppSharedPreferences
import com.example.quzbus.domain.repository.ResetUserDataRepository
import javax.inject.Inject

class ResetUserDataRepositoryImpl @Inject constructor(
    private val pref: AppSharedPreferences
) : ResetUserDataRepository {
    override fun setAccessToken(accessToken: String?) {
        pref.setAccessToken(accessToken)
    }

    override fun setPhoneNumber(phoneNumber: String?) {
        pref.setPhoneNumber(phoneNumber)
    }
}