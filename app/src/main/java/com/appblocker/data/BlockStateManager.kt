package com.appblocker.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BlockStateManager(context: Context, private val statsDao: StatsDao) {

    private val prefs = context.getSharedPreferences("block_state", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isBlocking = MutableStateFlow(prefs.getBoolean(KEY_BLOCKING, false))
    val isBlocking: StateFlow<Boolean> = _isBlocking.asStateFlow()

    fun toggle(): Boolean {
        val newState = !_isBlocking.value
        prefs.edit().putBoolean(KEY_BLOCKING, newState).apply()
        _isBlocking.value = newState

        scope.launch {
            val now = System.currentTimeMillis()
            if (newState) {
                statsDao.startSession(BlockingSession(startTime = now))
            } else {
                statsDao.endCurrentSession(now)
            }
        }

        return newState
    }

    fun restore() {
        _isBlocking.value = prefs.getBoolean(KEY_BLOCKING, false)
    }

    companion object {
        private const val KEY_BLOCKING = "is_blocking"
    }
}
