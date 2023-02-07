package com.example.quzbus.data.models.response.singlerouteresponse

import com.example.quzbus.domain.models.busroute.RoutesPoint

data class Pt(
    val X: Double,
    val Y: Double
) {

    fun toRoutesPoint(): RoutesPoint {
        return RoutesPoint(
            x = X,
            y = Y
        )
    }
}