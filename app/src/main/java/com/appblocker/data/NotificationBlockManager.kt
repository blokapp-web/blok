package com.appblocker.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages notification blocking preference.
 * Enabled by default — blocked apps have their notifications hidden.
 */
class NotificationBlockManager(context: Context) {

    private val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    private val _isEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, true))
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _isEnabled.value = enabled
    }

    companion object {
        private const val KEY_ENABLED = "block_notifications"
    }
}
