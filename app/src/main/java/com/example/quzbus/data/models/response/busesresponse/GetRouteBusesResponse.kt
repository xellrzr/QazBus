package com.example.quzbus.data.models.response.busesresponse

import com.example.quzbus.domain.models.buses.RouteBuses

data class GetRouteBusesResponse(
    val Lt: List<Lt>
) {

    fun toRouteBuses(): RouteBuses {
        return RouteBuses(
            busRoutes = Lt.map { it.toSingleBusRoute() }
        )
    }
}