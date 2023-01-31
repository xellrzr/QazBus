package com.example.quzbus.domain.repository

interface SaveDataRepository {

    fun setAccessToken(accessToken: String)

    fun setSelectCity(city: String)

    fun setPhoneNumber(phoneNumber: String)
}