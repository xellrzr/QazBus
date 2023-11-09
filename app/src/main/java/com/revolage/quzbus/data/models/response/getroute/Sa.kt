package com.example.quzbus.data.models.response.getroute

import com.example.quzbus.domain.models.routes.Stop

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