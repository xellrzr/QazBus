package com.example.quzbus.data.models.response.getroute

import com.example.quzbus.domain.models.routes.Point

data class La(
    val X: Double,
    val Y: Double
) {

    fun toRouteCoordinates(): Point {
        return Point(
            x = X,
            y = Y
        )
    }
}