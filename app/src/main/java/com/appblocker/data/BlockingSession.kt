package com.appblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocking_sessions")
data class BlockingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null
)
