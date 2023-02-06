package com.example.quzbus.domain.models.busroute

data class BusRoute(
    val routeA: List<RouteCoordinates>, //Маршрут А
    val routeB: List<RouteCoordinates>, //Маршрут Б
    val routeStart: String, //Старт маршрута
    val routeFinish: String, //Финиш маршрута
    val routeName: String, //Имя маршрута
    val routeStopsA: List<RouteStops>, //Остановки А
    val routeStopsB: List<RouteStops>, //Остановки Б
    val tp: String //Ничего
)
