package com.revolage.quzbus.ui.coordinates

import com.mapbox.geojson.Point

class CityCoordinates {

    companion object {
        fun cityCoordinates(city: String): Point {
            return when (city) {
                "Астана" -> Point.fromLngLat(71.447429, 51.168014)
                "Алматы" -> Point.fromLngLat(76.878096, 43.236352)
                "Актау" -> Point.fromLngLat(51.175112, 43.657283)
                "Актобе" -> Point.fromLngLat(57.171368, 50.283985)
                "Атырау" -> Point.fromLngLat(51.917083, 47.105050)
                "Аркалык" -> Point.fromLngLat(66.5441, 50.1455)
                "Есик" -> Point.fromLngLat(77.27, 43.21)
                "Каскелен" -> Point.fromLngLat(76.627544, 43.199252)
                "Костанай" -> Point.fromLngLat(63.640218, 53.212268)
                "Кызылорда" -> Point.fromLngLat(65.490363, 44.846396)
                "Петропавловск" -> Point.fromLngLat(69.149375, 54.863476)
                "Рудный" -> Point.fromLngLat(63.1168, 52.9729)
                "Талгар" -> Point.fromLngLat(77.240361, 43.302768)
                "Талдыкорган" -> Point.fromLngLat(78.380359, 45.015255)
                "Туркестан" -> Point.fromLngLat(68.239034, 43.304990)
                "Усть-Каменогорск" -> Point.fromLngLat(82.599988, 49.970557)
                "Шелек" -> Point.fromLngLat(78.1458, 43.3551)
                else -> Point.fromLngLat(71.447429, 51.168014)
            }
        }
    }
}