package com.example.quzbus.domain.repository

interface ResetUserDataRepository {

    fun setAccessToken(accessToken: String?)

    fun setPhoneNumber(phoneNumber: String?)
}