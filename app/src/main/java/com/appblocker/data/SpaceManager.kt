package com.appblocker.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages which Space is currently active.
 * When a space is activated, its apps are synced into blocked_apps table.
 */
class SpaceManager(
    context: Context,
    private val spaceDao: SpaceDao,
    private val blockedAppDao: BlockedAppDao
) {
    private val prefs = context.getSharedPreferences("space_state", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _activeSpaceId = MutableStateFlow(
        prefs.getLong(KEY_ACTIVE_SPACE, NO_SPACE).let { if (it == NO_SPACE) null else it }
    )
    val activeSpaceId: StateFlow<Long?> = _activeSpaceId.asStateFlow()

    private val _activeSpace = MutableStateFlow<Space?>(null)
    val activeSpace: StateFlow<Space?> = _activeSpace.asStateFlow()

    init {
        // Load active space details on init
        scope.launch {
            val id = _activeSpaceId.value
            if (id != null) {
                _activeSpace.value = spaceDao.getSpace(id)
            }
        }
    }

    /**
     * Activate a space: replace blocked_apps with this space's apps.
     */
    fun activateSpace(spaceId: Long) {
        prefs.edit().putLong(KEY_ACTIVE_SPACE, spaceId).apply()
        _activeSpaceId.value = spaceId
        scope.launch {
            _activeSpace.value = spaceDao.getSpace(spaceId)
            syncBlockedApps(spaceId)
        }
    }

    /**
     * Deactivate current space and clear blocked apps.
     */
    fun deactivateSpace() {
        prefs.edit().remove(KEY_ACTIVE_SPACE).apply()
        _activeSpaceId.value = null
        _activeSpace.value = null
        scope.launch {
            // Clear all blocked apps when no space is active
            val current = blockedAppDao.getAllBlockedSnapshot()
            current.forEach { blockedAppDao.deleteByPackage(it.packageName) }
        }
    }

    /**
     * Re-sync blocked_apps from the current active space (call after editing space apps).
     */
    fun refreshActiveSpace() {
        val id = _activeSpaceId.value ?: return
        scope.launch {
            _activeSpace.value = spaceDao.getSpace(id)
            syncBlockedApps(id)
        }
    }

    private suspend fun syncBlockedApps(spaceId: Long) {
        // Get space apps
        val spaceApps = spaceDao.getSpaceWithApps(spaceId)?.apps ?: return

        // Clear current blocked apps
        val current = blockedAppDao.getAllBlockedSnapshot()
        current.forEach { blockedAppDao.deleteByPackage(it.packageName) }

        // Insert space apps as blocked
        spaceApps.forEach { spaceApp ->
            blockedAppDao.insert(
                BlockedApp(
                    packageName = spaceApp.packageName,
                    appName = spaceApp.appName
                )
            )
        }
    }

    companion object {
        private const val KEY_ACTIVE_SPACE = "active_space_id"
        private const val NO_SPACE = -1L
    }
}
