package com.appblocker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {

    // ── Spaces ──

    @Query("SELECT * FROM spaces ORDER BY createdAt DESC")
    fun getAllSpaces(): Flow<List<Space>>

    @Transaction
    @Query("SELECT * FROM spaces ORDER BY createdAt DESC")
    fun getAllSpacesWithApps(): Flow<List<SpaceWithApps>>

    @Transaction
    @Query("SELECT * FROM spaces WHERE id = :spaceId")
    suspend fun getSpaceWithApps(spaceId: Long): SpaceWithApps?

    @Query("SELECT * FROM spaces WHERE id = :spaceId")
    suspend fun getSpace(spaceId: Long): Space?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpace(space: Space): Long

    @Update
    suspend fun updateSpace(space: Space)

    @Query("DELETE FROM spaces WHERE id = :spaceId")
    suspend fun deleteSpace(spaceId: Long)

    // ── Space Apps ──

    @Query("SELECT * FROM space_apps WHERE spaceId = :spaceId")
    fun getAppsForSpace(spaceId: Long): Flow<List<SpaceApp>>

    @Query("SELECT packageName FROM space_apps WHERE spaceId = :spaceId")
    suspend fun getPackagesForSpace(spaceId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpaceApp(spaceApp: SpaceApp)

    @Query("DELETE FROM space_apps WHERE spaceId = :spaceId AND packageName = :packageName")
    suspend fun removeSpaceApp(spaceId: Long, packageName: String)

    @Query("DELETE FROM space_apps WHERE spaceId = :spaceId")
    suspend fun clearSpaceApps(spaceId: Long)

    @Query("SELECT COUNT(*) FROM space_apps WHERE spaceId = :spaceId")
    fun getAppCount(spaceId: Long): Flow<Int>
}
