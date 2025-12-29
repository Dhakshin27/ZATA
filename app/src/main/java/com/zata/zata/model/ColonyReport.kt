package com.zata.zata.model
data class ColonyReport(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val isRockBee: Boolean = false,
    val confidence: Float = 0f,
    val timestamp: Long = 0L
)
