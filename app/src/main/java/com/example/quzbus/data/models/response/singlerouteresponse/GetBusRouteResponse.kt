package com.example.quzbus.data.models.response.singlerouteresponse

import com.example.quzbus.domain.models.busroute.BusRoute
import com.example.quzbus.domain.models.routes.Route

data class GetBusRouteResponse(
    val La: List<La>, //Маршрут А
    val Lb: List<Lb>, //Маршрут Б
    val Na: String, //Старт маршрута
    val Nb: String, //Финиш маршрута
    val Nm: String, //Имя маршрута
    val Sa: List<Sa>, //Остановки А
    val Sb: List<Sb>, //Остановки Б
    val Tp: String //Ничего
) {

    fun toBusRoute(): BusRoute {
        return BusRoute(
            routeA = La.map { it.toRouteCoordinates() },
            routeB = Lb.map { it.toRouteCoordinates() },
            routeStart = Na,
            routeFinish = Nb,
            routeName = Nm,
            routeStopsA = Sa.map { it.toRouteStops() },
            routeStopsB = Sb.map { it.toRouteStops() },
            tp = Tp
        )
    }

}