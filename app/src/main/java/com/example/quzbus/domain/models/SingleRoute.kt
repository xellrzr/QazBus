package com.example.quzbus.domain.models

import com.example.quzbus.data.models.response.busesresponse.Lt
import com.example.quzbus.data.models.response.singlerouteresponse.La
import com.example.quzbus.data.models.response.singlerouteresponse.Sa

data class SingleRoute(
    val routeA: List<La>, //Маршрут А
    val routeB: List<La>, //Маршрут Б
    val routeStopsA: List<Sa>, //Остановки А
    val routeStopsB: List<Sa>, //Остановки Б
    val routeStart: String, //Старт маршрута
    val routeFinish: String, //Финиш маршрута
    val busPoints: List<Lt>, //Автобусы
    val isFavorite: Boolean, // Избранное
    val name: String,
    val cityId: Int,
//    val selectedDirection: BusDirections? = null,
//    val pallet: Pallet
)
