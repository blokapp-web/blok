package com.appblocker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "space_apps",
    primaryKeys = ["spaceId", "packageName"],
    foreignKeys = [
        ForeignKey(
            entity = Space::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("spaceId")]
)
data class SpaceApp(
    val spaceId: Long,
    val packageName: String,
    val appName: String
)
