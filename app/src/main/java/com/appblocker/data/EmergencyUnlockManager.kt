package com.appblocker.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmergencyUnlockManager(context: Context) {

    private val prefs = context.getSharedPreferences("emergency_unlock", Context.MODE_PRIVATE)

    private val _remainingUses = MutableStateFlow(getRemainingUses())
    val remainingUses: StateFlow<Int> = _remainingUses.asStateFlow()

    private fun getRemainingUses(): Int {
        resetIfPeriodExpired()
        return prefs.getInt(KEY_REMAINING, MAX_USES)
    }

    fun canUnlock(): Boolean {
        resetIfPeriodExpired()
        return prefs.getInt(KEY_REMAINING, MAX_USES) > 0
    }

    /**
     * Usa un desbloqueo de emergencia. Devuelve true si se pudo usar.
     */
    fun useEmergencyUnlock(): Boolean {
        resetIfPeriodExpired()
        val remaining = prefs.getInt(KEY_REMAINING, MAX_USES)
        if (remaining <= 0) return false

        val newRemaining = remaining - 1
        prefs.edit()
            .putInt(KEY_REMAINING, newRemaining)
            .apply()

        // Guardar la primera vez que se usa en este periodo
        if (!prefs.contains(KEY_PERIOD_START)) {
            prefs.edit()
                .putLong(KEY_PERIOD_START, System.currentTimeMillis())
                .apply()
        }

        _remainingUses.value = newRemaining
        return true
    }

    fun getNextResetTimeMs(): Long {
        val periodStart = prefs.getLong(KEY_PERIOD_START, 0L)
        if (periodStart == 0L) return 0L
        return periodStart + PERIOD_MS
    }

    /**
     * Si han pasado 5 meses desde el inicio del periodo, resetea los usos.
     */
    private fun resetIfPeriodExpired() {
        val periodStart = prefs.getLong(KEY_PERIOD_START, 0L)
        if (periodStart == 0L) return

        if (System.currentTimeMillis() - periodStart >= PERIOD_MS) {
            prefs.edit()
                .putInt(KEY_REMAINING, MAX_USES)
                .remove(KEY_PERIOD_START)
                .apply()
            _remainingUses.value = MAX_USES
        }
    }

    companion object {
        private const val KEY_REMAINING = "remaining_uses"
        private const val KEY_PERIOD_START = "period_start"
        const val MAX_USES = 3
        // 1 mes en milisegundos (~30 dias)
        const val PERIOD_MS = 30L * 24L * 60L * 60L * 1000L
    }
}
