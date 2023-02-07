package com.example.quzbus.data.models.response.getroute

import com.example.quzbus.domain.models.routes.RouteCoordinates

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