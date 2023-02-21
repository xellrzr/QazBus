package com.example.quzbus.ui.viewmodels

import com.example.quzbus.domain.models.routes.Pallet
import com.example.quzbus.domain.models.routes.Route

data class RouteState(
    val route: Route? = null,
    val pallet: Pallet? = null,
    val event: Event,
)

enum class Event {
    CLEAR, BUS, ROUTE, REDRAW
}
