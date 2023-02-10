package com.example.quzbus.domain.models.routes

import com.example.quzbus.data.models.response.getbuses.GetBusesResponse
import com.example.quzbus.data.models.response.getroute.GetRouteResponse
import com.example.quzbus.domain.models.bus.Bus

data class Route(
    val name: String,
    val auto: String,
    var routeA: List<Point> = emptyList(), //Маршрут А
    var routeB: List<Point> = emptyList(), //Маршрут Б
    var routeStopsA: List<Stop> = emptyList(), //Остановки А
    var routeStopsB: List<Stop> = emptyList(), //Остановки Б
    var routeStart: String? = null, //Старт маршрута
    var routeFinish: String? = null, //Финиш маршрута
    var busPoints: List<Bus> = emptyList(), //Автобусы
    var isFavorite: Boolean = false, // Избранное
    var selectedDirection: Direction? = null,
    var pallet: Pallet? = null,
    var isSelected: Boolean = false,
) {

    fun fillFrom(response: GetRouteResponse) {
        routeA = response.La.map { it.toRouteCoordinates() }
        routeB = response.Lb.map { it.toRouteCoordinates() }
        routeStopsA = response.Sa.map { it.toStop() }
        routeStopsB = response.Sb.map { it.toRouteStops() }
        routeStart = response.Na
        routeFinish = response.Nb
    }

    fun fillBusesFrom(response: GetBusesResponse) {
        busPoints = response.Lt.map { it.toBus() }
    }

    fun reset() {
        routeA = emptyList()
        routeB = emptyList()
        busPoints = emptyList()
        routeStopsA = emptyList()
        routeStopsB = emptyList()
        selectedDirection = null
        isSelected = false
    }
}
