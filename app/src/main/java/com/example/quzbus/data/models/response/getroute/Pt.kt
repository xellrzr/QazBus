package com.example.quzbus.data.models.response.getroute

import com.example.quzbus.domain.models.routes.RoutePoint

data class Pt(
    val X: Double,
    val Y: Double
) {

    fun toRoutesPoint(): RoutePoint {
        return RoutePoint(
            x = X,
            y = Y
        )
    }
}