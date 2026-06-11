package com.appblocker.model

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false
)
