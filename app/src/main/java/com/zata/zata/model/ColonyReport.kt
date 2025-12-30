package com.zata.zata.model

data class ColonyReport(
    val rockBee: Boolean = false,
    val confidence: Double = 0.0,
    val timestamp: Long = 0L,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

