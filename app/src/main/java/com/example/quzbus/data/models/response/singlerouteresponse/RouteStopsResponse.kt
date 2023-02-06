package com.example.quzbus.data.models.response.singlerouteresponse

import com.example.quzbus.domain.models.busroute.RouteStops

data class RouteStopsResponse(
    val routeName: String,
    val routePoint: RoutesPointResponse
) {

    fun toRouteStops(): RouteStops {
        return RouteStops(
            routeName = routeName,
            routePoint = routePoint.toRoutesPoint()
        )
    }
}