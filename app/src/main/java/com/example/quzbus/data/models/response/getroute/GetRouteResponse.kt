package com.example.quzbus.data.models.response.getroute

data class GetRouteResponse(
    val La: List<La>, //Маршрут А
    val Lb: List<Lb>, //Маршрут Б
    val Na: String, //Старт маршрута
    val Nb: String, //Финиш маршрута
    val Nm: String, //Имя маршрута
    val Sa: List<Sa>, //Остановки А
    val Sb: List<Sb>, //Остановки Б
    val Tp: String //Ничего
)