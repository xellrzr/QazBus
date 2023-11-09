package com.revolage.quzbus.data.models.response.getroutes

import com.revolage.quzbus.domain.models.routes.Routes

data class GetRoutesResponse(
    val routes: List<RouteResponse>
) {

    fun toRoutes(): Routes {
        return Routes(
            routes = routes.map { it.toRoute() }
        )
    }
}
