package com.revolage.quzbus.domain.repository

interface FavoriteRouteRepository {

    fun getFavoriteRoute(route: String, cityId: Int): Boolean
    fun setFavoriteRoute(route: String, cityId: Int, isFavorite: Boolean)
}