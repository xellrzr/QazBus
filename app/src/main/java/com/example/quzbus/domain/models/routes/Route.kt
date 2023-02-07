package com.example.quzbus.domain.models.routes

import com.example.quzbus.domain.models.bus.Bus

data class Route(
    val name: String,
    val auto: String,
    var routeA: List<RouteCoordinates> = emptyList(), //Маршрут А
    var routeB: List<RouteCoordinates> = emptyList(), //Маршрут Б
    var routeStopsA: List<RouteStops> = emptyList(), //Остановки А
    var routeStopsB: List<RouteStops> = emptyList(), //Остановки Б
    var routeStart: String? = null, //Старт маршрута
    var routeFinish: String? = null, //Финиш маршрута
    val busPoints: List<Bus> = emptyList(), //Автобусы
    val isFavorite: Boolean = false, // Избранное
)
