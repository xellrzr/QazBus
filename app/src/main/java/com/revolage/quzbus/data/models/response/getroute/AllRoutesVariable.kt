package com.revolage.quzbus.data.models.response.getroute

import com.revolage.quzbus.domain.models.routes.Point
import com.revolage.quzbus.domain.models.routes.Stop

data class La(
    val X: Double,
    val Y: Double
) {
    fun toRouteCoordinates(): Point {
        return Point(
            x = X,
            y = Y
        )
    }
}

data class Lb(
    val X: Double,
    val Y: Double
) {
    fun toRouteCoordinates(): Point {
        return Point(
            x = X,
            y = Y
        )
    }
}

data class Pt(
    val X: Double,
    val Y: Double
) {
    fun toPoint(): Point {
        return Point(
            x = X,
            y = Y
        )
    }
}

data class Sa(
    val Nm: String,
    val Pt: Pt
) {
    fun toStop(): Stop {
        return Stop(
            name = Nm,
            point = Pt.toPoint()
        )
    }
}

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
