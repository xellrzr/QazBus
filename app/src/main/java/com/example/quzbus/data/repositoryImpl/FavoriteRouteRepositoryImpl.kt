package com.example.quzbus.data.repositoryImpl

import com.example.quzbus.data.sharedpref.AppSharedPreferences
import com.example.quzbus.domain.repository.FavoriteRouteRepository
import javax.inject.Inject

class FavoriteRouteRepositoryImpl @Inject constructor(
    private val pref: AppSharedPreferences
) : FavoriteRouteRepository {

    override fun getFavoriteRoute(route: String, cityId: Int): Boolean {
        return pref.getFavoriteRoute(route, cityId)
    }

    override fun setFavoriteRoute(route: String, cityId: Int, isFavorite: Boolean) {
        pref.setFavoriteRoute(route, cityId, isFavorite)
    }
}