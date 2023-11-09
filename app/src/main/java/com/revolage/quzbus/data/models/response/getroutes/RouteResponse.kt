package com.revolage.quzbus.data.models.response.getroutes

import com.revolage.quzbus.domain.models.routes.Route

data class RouteResponse(
    val route: String,
    val auto: String
) {

    fun toRoute(): Route {
        return Route(
            name = route,
            auto = auto
        )
    }
}
