package com.example.quzbus.data.models.response.singlerouteresponse

import com.example.quzbus.domain.models.busroute.RoutesPoint

data class RoutesPointResponse(
    val x: Double,
    val y: Double
) {

    fun toRoutesPoint(): RoutesPoint {
        return RoutesPoint(
            x = x,
            y = y
        )
    }
}