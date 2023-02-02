package com.example.quzbus.data.models.response.singleroute

data class BusRoute(
    val RouteA: List<RouteA>, //Маршрут А
    val RouteB: List<RouteB>, //Маршрут Б
    val RouteStart: String, //Старт маршрута
    val RouteFinish: String, //Финиш маршрута
    val RouteName: String, //Имя маршрута
    val RouteStopsA: List<RouteStopsA>, //Остановки
    val RouteStopsB: List<RouteStopsB>, //Остановки
    val Tp: String //Ничего
)