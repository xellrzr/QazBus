package com.example.quzbus.domain.models

data class BusPointModel(
    val transportNumber: String, // Номер автомобиля
    val direction: Int, // Направление движения, может быть 1 или 2
    val transportId: String, //Идентификатор транспорта
    val Tp: Int, //Ничего
    val longitude1: Double,
    val longitude2: Double,
    val latitude1: Double,
    val latitude2: Double
)
