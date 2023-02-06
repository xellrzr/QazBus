package com.example.quzbus.data.models.response.singlerouteresponse

import com.example.quzbus.domain.models.busroute.RouteCoordinates

data class RouteCoordinatesResponse(
    val x: Double,
    val y: Double
) {

    fun toRouteCoordinates(): RouteCoordinates {
        return RouteCoordinates(
            x = x,
            y = y
        )
    }
}