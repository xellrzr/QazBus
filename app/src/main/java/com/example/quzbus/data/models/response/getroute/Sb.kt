package com.example.quzbus.data.models.response.getroute

import com.example.quzbus.domain.models.routes.RouteStops

data class Sb(
    val Nm: String,
    val Pt: Pt
) {

    fun toRouteStops(): RouteStops {
        return RouteStops(
            routeName = Nm,
            routePoint = Pt.toRoutesPoint()
        )
    }
}
