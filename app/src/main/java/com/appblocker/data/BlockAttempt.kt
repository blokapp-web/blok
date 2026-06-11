package com.appblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_attempts")
data class BlockAttempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis()
)
