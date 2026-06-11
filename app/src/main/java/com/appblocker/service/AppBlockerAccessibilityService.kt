package com.appblocker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import com.appblocker.AppBlockerApplication
import com.appblocker.data.BlockAttempt
import com.appblocker.ui.screens.BlockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppBlockerAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var blockedPackages: Set<String> = emptySet()
    private var lastBlockTime = 0L
    private var lastBlockedPkg = ""

    private val systemPackages = setOf(
        "com.android.launcher",
        "com.sec.android.app.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.android.systemui",
        "com.samsung.android.app.routines",
        "com.android.settings"
    )

    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 30
        }

        val app = application as AppBlockerApplication
        scope.launch {
            app.database.blockedAppDao().getAllBlockedPackages().collect { packages ->
                blockedPackages = packages.toSet()
            }
        }

        instance = this
        _isRunning.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName == applicationContext.packageName) return
        if (packageName in systemPackages) return

        val app = application as AppBlockerApplication
        val isBlocking = app.blockStateManager.isBlocking.value

        if (isBlocking && packageName in blockedPackages) {
            val now = System.currentTimeMillis()
            if (now - lastBlockTime < 500 && packageName == lastBlockedPkg) return
            lastBlockTime = now
            lastBlockedPkg = packageName

            val appName = getAppName(packageName)
            scope.launch(Dispatchers.IO) {
                app.database.statsDao().logAttempt(
                    BlockAttempt(packageName = packageName, appName = appName)
                )
            }

            val intent = Intent(this, BlockOverlayActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
                putExtra("app_name", appName)
                putExtra("package_name", packageName)
            }
            startActivity(intent)
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val ai = applicationContext.packageManager.getApplicationInfo(packageName, 0)
            applicationContext.packageManager.getApplicationLabel(ai).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        instance = null
        _isRunning.value = false
        super.onDestroy()
    }

    companion object {
        var instance: AppBlockerAccessibilityService? = null
            private set

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    }
}
