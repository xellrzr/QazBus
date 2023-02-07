package com.example.quzbus.data.models.response.singlerouteresponse

import com.example.quzbus.domain.models.busroute.RouteCoordinates

data class La(
    val X: Double,
    val Y: Double
) {

    fun toRouteCoordinates(): RouteCoordinates {
        return RouteCoordinates(
            x = X,
            y = Y
        )
    }
}