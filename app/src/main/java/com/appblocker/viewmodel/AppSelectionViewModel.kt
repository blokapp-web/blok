package com.appblocker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appblocker.model.InstalledApp
import com.appblocker.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Apps filtradas por búsqueda, con su estado blocked actualizado en tiempo real */
    val filteredApps: StateFlow<List<InstalledApp>> = combine(
        _installedApps,
        repository.getBlockedPackages(),
        _searchQuery
    ) { apps, blockedPackages, query ->
        val blockedSet = blockedPackages.toSet()
        apps
            .map { it.copy(isBlocked = it.packageName in blockedSet) }
            .filter { query.isBlank() || it.appName.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val blockedCount: StateFlow<Int> = repository.getBlockedPackages()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Names of blocked apps for display chips */
    val blockedAppNames: StateFlow<List<String>> = combine(
        _installedApps,
        repository.getBlockedPackages()
    ) { apps, blockedPackages ->
        val blockedSet = blockedPackages.toSet()
        apps.filter { it.packageName in blockedSet }.map { it.appName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            _installedApps.value = repository.getInstalledApps()
            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleApp(app: InstalledApp) {
        viewModelScope.launch {
            repository.toggleBlock(app)
        }
    }
}
