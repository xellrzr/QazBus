package com.example.quzbus.domain.models.buses

data class RouteBusesState(
    val busRoutes: List<SingleBusRoute> = emptyList(),
    val isLoading: Boolean = false
)
