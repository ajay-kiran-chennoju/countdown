package com.example.progresswidget.model

data class BatteryEvent(
    val id: String,
    val title: String,
    val description: String,
    val defaultTtsText: String,
    val ttsText: String = defaultTtsText,
    val isEnabled: Boolean = false,
    val threshold: Int? = null,
    val lastTriggered: Long = 0L
)
