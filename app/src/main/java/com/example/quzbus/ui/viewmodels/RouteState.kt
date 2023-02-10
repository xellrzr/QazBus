package com.example.quzbus.ui.viewmodels

import com.example.quzbus.domain.models.routes.Route

data class RouteState(
    val routes: HashMap<String, Route> = hashMapOf(),
    val error: String? = null,
    val isLoading: Boolean = true
)
