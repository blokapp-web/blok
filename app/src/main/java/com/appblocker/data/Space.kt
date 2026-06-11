package com.appblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spaces")
data class Space(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String,       // Material icon key, e.g. "Work", "School"
    val createdAt: Long = System.currentTimeMillis()
)
