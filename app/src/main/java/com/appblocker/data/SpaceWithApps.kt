package com.appblocker.data

import androidx.room.Embedded
import androidx.room.Relation

data class SpaceWithApps(
    @Embedded val space: Space,
    @Relation(
        parentColumn = "id",
        entityColumn = "spaceId"
    )
    val apps: List<SpaceApp>
)
