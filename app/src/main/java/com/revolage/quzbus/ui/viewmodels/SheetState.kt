package com.revolage.quzbus.ui.viewmodels

import com.revolage.quzbus.data.models.response.Region
import com.revolage.quzbus.domain.models.routes.Route

data class SheetState(
    val isAuthorized: Boolean = false,
    val isCitySelected: Boolean = false,
    val cities: List<Region> = emptyList(),
    val routes: List<Route> = emptyList(),
    val error: String? = null
)
