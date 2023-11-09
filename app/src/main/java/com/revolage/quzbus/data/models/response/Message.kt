package com.revolage.quzbus.data.models.response

data class Message(
    val result: String,
    val regions: List<Region>,
)