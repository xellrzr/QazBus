package com.example.quzbus.data.models.response.getroute

import com.example.quzbus.domain.models.routes.Point

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