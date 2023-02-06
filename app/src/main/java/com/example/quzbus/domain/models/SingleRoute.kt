package com.example.quzbus.domain.models

import com.example.quzbus.data.models.response.bus.BusPointResponse
import com.example.quzbus.data.models.response.singlerouteresponse.RouteCoordinatesResponse
import com.example.quzbus.data.models.response.singlerouteresponse.RouteStopsResponse

data class SingleRoute(
    val routeA: List<RouteCoordinatesResponse>, //Маршрут А
    val routeB: List<RouteCoordinatesResponse>, //Маршрут Б
    val routeStopsA: List<RouteStopsResponse>, //Остановки А
    val routeStopsB: List<RouteStopsResponse>, //Остановки Б
    val routeStart: String, //Старт маршрута
    val routeFinish: String, //Финиш маршрута
    val busPoints: List<BusPointResponse>, //Автобусы
    val isFavorite: Boolean, // Избранное
    val name: String,
    val cityId: Int,
//    val selectedDirection: BusDirections? = null,
//    val pallet: Pallet
)
