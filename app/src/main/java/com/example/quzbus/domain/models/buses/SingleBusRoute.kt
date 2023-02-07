package com.example.quzbus.domain.models.buses

data class SingleBusRoute(
    val transportNumber: String,
    val direction: Int,
    val transportId: String,
    val tp: Int, //Ничего
    val longitude1: Double,
    val longitude2: Double,
    val latitude1: Double,
    val latitude2: Double
)
