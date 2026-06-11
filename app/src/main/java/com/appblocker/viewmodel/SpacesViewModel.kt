package com.appblocker.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appblocker.AppBlockerApplication
import com.appblocker.data.Space
import com.appblocker.data.SpaceApp
import com.appblocker.data.SpaceWithApps
import com.appblocker.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpacesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AppBlockerApplication
    private val spaceDao = app.database.spaceDao()
    val spaceManager = app.spaceManager

    /** All spaces */
    val spaces = spaceDao.getAllSpacesWithApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Editor state ──
    private val _editorSpaceId = MutableStateFlow<Long?>(null)
    val editorSpaceId: StateFlow<Long?> = _editorSpaceId.asStateFlow()

    private val _editorName = MutableStateFlow("")
    val editorName: StateFlow<String> = _editorName.asStateFlow()

    private val _editorIcon = MutableStateFlow("Work")
    val editorIcon: StateFlow<String> = _editorIcon.asStateFlow()

    private val _editorSelectedPackages = MutableStateFlow<Set<String>>(emptySet())
    val editorSelectedPackages: StateFlow<Set<String>> = _editorSelectedPackages.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Start creating a new space */
    fun startNewSpace() {
        _editorSpaceId.value = null
        _editorName.value = ""
        _editorIcon.value = "Work"
        _editorSelectedPackages.value = emptySet()
        _searchQuery.value = ""
        loadInstalledApps()
    }

    /** Start editing existing space */
    fun startEditSpace(spaceId: Long) {
        _editorSpaceId.value = spaceId
        _searchQuery.value = ""
        viewModelScope.launch {
            val spaceWithApps = spaceDao.getSpaceWithApps(spaceId)
            if (spaceWithApps != null) {
                _editorName.value = spaceWithApps.space.name
                _editorIcon.value = spaceWithApps.space.iconName
                _editorSelectedPackages.value = spaceWithApps.apps.map { it.packageName }.toSet()
            }
        }
        loadInstalledApps()
    }

    fun setEditorName(name: String) { _editorName.value = name }
    fun setEditorIcon(icon: String) { _editorIcon.value = icon }
    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }

    fun toggleAppInEditor(app: InstalledApp) {
        val current = _editorSelectedPackages.value.toMutableSet()
        if (app.packageName in current) {
            current.remove(app.packageName)
        } else {
            current.add(app.packageName)
        }
        _editorSelectedPackages.value = current
    }

    /** Save space (create or update) */
    fun saveSpace(onComplete: () -> Unit) {
        val name = _editorName.value.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            val existingId = _editorSpaceId.value
            val spaceId: Long

            if (existingId != null) {
                // Update
                spaceDao.updateSpace(
                    Space(id = existingId, name = name, iconName = _editorIcon.value)
                )
                spaceId = existingId
                spaceDao.clearSpaceApps(spaceId)
            } else {
                // Create
                spaceId = spaceDao.insertSpace(
                    Space(name = name, iconName = _editorIcon.value)
                )
            }

            // Insert selected apps
            val pm = getApplication<Application>().packageManager
            _editorSelectedPackages.value.forEach { pkg ->
                val appName = try {
                    val ai = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(ai).toString()
                } catch (_: PackageManager.NameNotFoundException) { pkg }

                spaceDao.insertSpaceApp(SpaceApp(spaceId = spaceId, packageName = pkg, appName = appName))
            }

            // If this space is active, refresh blocked apps
            if (spaceManager.activeSpaceId.value == spaceId) {
                spaceManager.refreshActiveSpace()
            }

            onComplete()
        }
    }

    fun deleteSpace(spaceId: Long) {
        viewModelScope.launch {
            // If active, deactivate first
            if (spaceManager.activeSpaceId.value == spaceId) {
                spaceManager.deactivateSpace()
            }
            spaceDao.deleteSpace(spaceId)
        }
    }

    fun activateSpace(spaceId: Long) {
        spaceManager.activateSpace(spaceId)
    }

    fun deactivateSpace() {
        spaceManager.deactivateSpace()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                val pm = getApplication<Application>().packageManager
                val ownPkg = getApplication<Application>().packageName
                val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
                    .asSequence()
                    .map { it.activityInfo }
                    .filter { it.packageName != ownPkg }
                    .distinctBy { it.packageName }
                    .map { info ->
                        InstalledApp(
                            packageName = info.packageName,
                            appName = info.loadLabel(pm).toString()
                        )
                    }
                    .sortedBy { it.appName.lowercase() }
                    .toList()
            }
            _installedApps.value = apps
            _isLoading.value = false
        }
    }
}
