package com.example.quzbus.domain.models

import com.example.quzbus.domain.models.routes.Route

data class RouteState(
    val routes: HashMap<String, Route> = hashMapOf(),
    val error: String? = null,
    val isLoading: Boolean = false
)
