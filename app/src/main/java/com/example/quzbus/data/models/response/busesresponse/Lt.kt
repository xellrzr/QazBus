package com.example.quzbus.data.models.response.busesresponse

import com.example.quzbus.domain.models.buses.SingleBusRoute

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

    fun toSingleBusRoute(): SingleBusRoute {
        return SingleBusRoute(
            transportNumber = An,
            direction = Dr,
            transportId = Id,
            tp = Tp,
            longitude1 = X1,
            longitude2 = X2,
            latitude1 = Y1,
            latitude2 = Y2
        )
    }
}