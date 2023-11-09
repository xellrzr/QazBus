package com.revolage.quzbus.data.models.response.getroute

import com.revolage.quzbus.domain.models.routes.Point

data class Pt(
    val X: Double,
    val Y: Double
) {

    fun toPoint(): Point {
        return Point(
            x = X,
            y = Y
        )
    }
}