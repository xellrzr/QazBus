package com.example.quzbus.data.models.response.routesresponse

import com.example.quzbus.domain.models.routes.Routes

data class GetRoutesResponse(
    val routes: List<RouteResponse>
) {

    fun toRoutes(): Routes {
        return Routes(
            routes = routes.map { it.toRoute() }
        )
    }
}
