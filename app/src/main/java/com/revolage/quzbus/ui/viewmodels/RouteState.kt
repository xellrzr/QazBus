package com.revolage.quzbus.ui.viewmodels

import com.revolage.quzbus.domain.models.routes.Pallet
import com.revolage.quzbus.domain.models.routes.Route

data class RouteState(
    val route: Route? = null,
    val pallet: Pallet? = null,
    val event: Event,
    val showStops: Boolean = false
)

enum class Event {
    CLEAR, BUS, ROUTE, REDRAW, ZOOM
}
