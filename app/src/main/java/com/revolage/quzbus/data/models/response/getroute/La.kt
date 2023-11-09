package com.revolage.quzbus.data.models.response.getroute

import com.revolage.quzbus.domain.models.routes.Point

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