package com.example.quzbus.data.models.response.singlerouteresponse

import com.example.quzbus.domain.models.busroute.BusRoute

data class GetBusRouteResponse(
    val routeA: List<RouteCoordinatesResponse>, //Маршрут А
    val routeB: List<RouteCoordinatesResponse>, //Маршрут Б
    val routeStart: String, //Старт маршрута
    val routeFinish: String, //Финиш маршрута
    val routeName: String, //Имя маршрута
    val routeStopsA: List<RouteStopsResponse>, //Остановки А
    val routeStopsB: List<RouteStopsResponse>, //Остановки Б
    val tp: String //Ничего
) {

    fun toBusRoute(): BusRoute {
        return BusRoute(
            routeA = routeA.map { it.toRouteCoordinates() },
            routeB = routeB.map { it.toRouteCoordinates() },
            routeStart = routeStart,
            routeFinish = routeFinish,
            routeName = routeName,
            routeStopsA = routeStopsA.map { it.toRouteStops() },
            routeStopsB = routeStopsB.map { it.toRouteStops() },
            tp = tp
        )
    }
}