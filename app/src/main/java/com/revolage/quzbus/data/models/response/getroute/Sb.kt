package com.revolage.quzbus.data.models.response.getroute

import com.revolage.quzbus.domain.models.routes.Stop

data class Sb(
    val Nm: String,
    val Pt: Pt
) {

    fun toRouteStops(): Stop {
        return Stop(
            name = Nm,
            point = Pt.toPoint()
        )
    }
}
