package com.example.quzbus.domain.models.bus

import com.example.quzbus.domain.models.routes.Direction
import com.example.quzbus.domain.models.routes.Point

data class Bus(
    val transportNumber: String,
    val direction: Direction,
    val transportId: String,
    val tp: Int, //Ничего
    val pointA: Point,
    val pointB: Point
)
