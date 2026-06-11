package com.appblocker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

data class AppAttemptCount(
    val packageName: String,
    val appName: String,
    val count: Int
)

@Dao
interface StatsDao {

    @Insert
    suspend fun startSession(session: BlockingSession)

    @Query("UPDATE blocking_sessions SET endTime = :endTime WHERE endTime IS NULL")
    suspend fun endCurrentSession(endTime: Long)

    @Insert
    suspend fun logAttempt(attempt: BlockAttempt)

    @Query("SELECT * FROM blocking_sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<BlockingSession>

    @Query(
        """SELECT * FROM blocking_sessions
           WHERE startTime >= :since
              OR (startTime < :since AND (endTime IS NULL OR endTime >= :since))
           ORDER BY startTime DESC"""
    )
    suspend fun getSessionsOverlapping(since: Long): List<BlockingSession>

    @Query("SELECT COUNT(*) FROM block_attempts WHERE timestamp >= :since")
    suspend fun getTotalAttemptsSince(since: Long): Int

    @Query(
        """SELECT packageName, appName, COUNT(*) as count
           FROM block_attempts
           WHERE timestamp >= :since
           GROUP BY packageName
           ORDER BY count DESC"""
    )
    suspend fun getAttemptCountsByApp(since: Long): List<AppAttemptCount>

    @Query(
        """SELECT packageName, appName, COUNT(*) as count
           FROM block_attempts
           WHERE timestamp >= :dayStart AND timestamp < :dayEnd
           GROUP BY packageName
           ORDER BY count DESC"""
    )
    suspend fun getAttemptCountsByAppForDay(dayStart: Long, dayEnd: Long): List<AppAttemptCount>

    @Query("SELECT COUNT(*) FROM block_attempts WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    suspend fun getAttemptsForDay(dayStart: Long, dayEnd: Long): Int
}
