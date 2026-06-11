package com.appblocker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appblocker.data.AppAttemptCount
import com.appblocker.data.AppDatabase
import com.appblocker.data.StatsDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class DayStats(
    val date: LocalDate,
    val attempts: Int,
    val hadSession: Boolean,
    val blockedMinutes: Long = 0
)

data class StatsState(
    val streak: Int = 0,
    val todayBlockedMinutes: Long = 0,
    val todayAttempts: Int = 0,
    val topApps: List<AppAttemptCount> = emptyList(),
    val weeklyData: List<DayStats> = emptyList(),
    val monthData: List<DayStats> = emptyList(),
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now(),
    val displayedMonth: YearMonth = YearMonth.now()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: StatsDao = AppDatabase.getInstance(application).statsDao()
    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            loadAll(_state.value.selectedDate)
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    /** Select a day — update UI immediately, load data in background without spinner */
    fun selectDate(date: LocalDate) {
        // Instant UI update
        _state.value = _state.value.copy(selectedDate = date, displayedMonth = YearMonth.from(date))
        // Background data load
        viewModelScope.launch { loadDayData(date) }
    }

    /** Change month — update UI immediately, load month grid in background */
    fun changeMonth(month: YearMonth) {
        _state.value = _state.value.copy(displayedMonth = month)
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val monthData = computeMonthData(month, zone)
            _state.value = _state.value.copy(monthData = monthData)
        }
    }

    private suspend fun loadAll(date: LocalDate) {
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val streak = computeStreak()
        val blockedMinutes = computeBlockedMinutesForDay(dayStart, minOf(dayEnd, now))
        val dayAttempts = dao.getAttemptsForDay(dayStart, dayEnd)
        val topApps = dao.getAttemptCountsByAppForDay(dayStart, dayEnd)
        val weeklyData = computeWeeklyData(zone)
        val monthData = computeMonthData(_state.value.displayedMonth, zone)

        _state.value = _state.value.copy(
            streak = streak,
            todayBlockedMinutes = blockedMinutes,
            todayAttempts = dayAttempts,
            topApps = topApps,
            weeklyData = weeklyData,
            monthData = monthData,
            selectedDate = date
        )
    }

    /** Lightweight load — only day-specific data, no weekly/monthly recalc */
    private suspend fun loadDayData(date: LocalDate) {
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val blockedMinutes = computeBlockedMinutesForDay(dayStart, minOf(dayEnd, now))
        val dayAttempts = dao.getAttemptsForDay(dayStart, dayEnd)
        val topApps = dao.getAttemptCountsByAppForDay(dayStart, dayEnd)

        _state.value = _state.value.copy(
            todayBlockedMinutes = blockedMinutes,
            todayAttempts = dayAttempts,
            topApps = topApps
        )
    }

    private suspend fun computeStreak(): Int {
        val sessions = dao.getAllSessions()
        if (sessions.isEmpty()) return 0
        val zone = ZoneId.systemDefault()
        val activeDays = mutableSetOf<LocalDate>()
        for (s in sessions) {
            val start = Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate()
            val end = if (s.endTime != null) Instant.ofEpochMilli(s.endTime).atZone(zone).toLocalDate() else LocalDate.now()
            var d = start
            while (!d.isAfter(end) && !d.isAfter(LocalDate.now())) { activeDays.add(d); d = d.plusDays(1) }
        }
        var streak = 0; var d = LocalDate.now()
        while (d in activeDays) { streak++; d = d.minusDays(1) }
        return streak
    }

    private suspend fun computeBlockedMinutesForDay(dayStart: Long, dayEnd: Long): Long {
        val sessions = dao.getSessionsOverlapping(dayStart)
        var totalMs = 0L
        for (s in sessions) {
            val start = maxOf(s.startTime, dayStart)
            val end = s.endTime?.let { minOf(it, dayEnd) } ?: dayEnd
            totalMs += maxOf(0, end - start)
        }
        return totalMs / 60_000
    }

    private suspend fun computeWeeklyData(zone: ZoneId): List<DayStats> {
        val sessions = dao.getAllSessions()
        val activeDays = buildActiveDays(sessions, zone)
        val today = LocalDate.now(); val now = System.currentTimeMillis()
        return (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            DayStats(
                date = date,
                attempts = dao.getAttemptsForDay(dayStart, dayEnd),
                hadSession = date in activeDays,
                blockedMinutes = computeBlockedMinutesForDay(dayStart, minOf(dayEnd, now))
            )
        }
    }

    private suspend fun computeMonthData(month: YearMonth, zone: ZoneId): List<DayStats> {
        val sessions = dao.getAllSessions()
        val activeDays = buildActiveDays(sessions, zone)
        val now = System.currentTimeMillis()
        return (1..month.lengthOfMonth()).map { dayNum ->
            val date = month.atDay(dayNum)
            if (date.isAfter(LocalDate.now())) {
                DayStats(date = date, attempts = 0, hadSession = false, blockedMinutes = 0)
            } else {
                val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
                val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                DayStats(
                    date = date,
                    attempts = dao.getAttemptsForDay(dayStart, dayEnd),
                    hadSession = date in activeDays,
                    blockedMinutes = computeBlockedMinutesForDay(dayStart, minOf(dayEnd, now))
                )
            }
        }
    }

    private fun buildActiveDays(sessions: List<com.appblocker.data.BlockingSession>, zone: ZoneId): Set<LocalDate> {
        val activeDays = mutableSetOf<LocalDate>()
        for (s in sessions) {
            val start = Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate()
            val end = if (s.endTime != null) Instant.ofEpochMilli(s.endTime).atZone(zone).toLocalDate() else LocalDate.now()
            var d = start
            while (!d.isAfter(end) && !d.isAfter(LocalDate.now())) { activeDays.add(d); d = d.plusDays(1) }
        }
        return activeDays
    }
}
