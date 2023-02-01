package com.example.quzbus.data.sharedpref

import android.content.SharedPreferences
import javax.inject.Inject

class AppSharedPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun getAccessToken(): String? = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)

    fun setAccessToken(accessToken: String) {
        sharedPreferences.edit().putString(ACCESS_TOKEN_KEY, accessToken).apply()
    }

    fun getSelectCity(): String? = sharedPreferences.getString(SELECT_CITY, null)

    fun setSelectCity(city: String) {
        sharedPreferences.edit().putString(SELECT_CITY, city).apply()
    }

    fun getSelectCityId(): Int = sharedPreferences.getInt(CITY_ID, 0)

    fun setCityId(cityId: Int) {
        sharedPreferences.edit().putInt(CITY_ID, cityId).apply()
    }

    fun getPhoneNumber(): String? = sharedPreferences.getString(PHONE_NUMBER, null)

    fun setPhoneNumber(phoneNumber: String) {
        sharedPreferences.edit().putString(PHONE_NUMBER, phoneNumber).apply()
    }

    companion object {
        const val SHARED_PREFS = "APP_SHARED_PREFS"
        const val ACCESS_TOKEN_KEY = "access_token"
        const val SELECT_CITY = "select_city"
        const val PHONE_NUMBER = "phone_number"
        const val CITY_ID = "city_id"
    }
}