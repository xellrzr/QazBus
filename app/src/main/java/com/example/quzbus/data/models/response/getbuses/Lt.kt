package com.example.quzbus.data.models.response.getbuses

import com.example.quzbus.domain.models.bus.Bus
import com.example.quzbus.domain.models.routes.Direction
import com.example.quzbus.domain.models.routes.Point

data class Lt(
    val An: String, // Номер автомобиля
    val Dr: Int, // Направление движения, может быть 1 или 2
    val Id: String, //Идентификатор транспорта
    val Tp: Int, //Ничего
    val X1: Double, //Longitude 1
    val X2: Double, // Longitude 2
    val Y1: Double, // Latitude 1
    val Y2: Double // Latitude 2
) {

    fun toBus(): Bus {
        return Bus(
            transportNumber = An,
            direction = if (Dr == 1) Direction.DIRECTION_A else Direction.DIRECTION_B,
            transportId = Id,
            tp = Tp,
            pointA = Point(X1, Y1),
            pointB = Point(X2, Y2)
        )
    }
}