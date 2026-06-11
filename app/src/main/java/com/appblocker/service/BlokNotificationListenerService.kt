package com.appblocker.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.appblocker.AppBlockerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Listens for notifications and cancels them if they come from blocked apps
 * while blocking is active and notification blocking is enabled.
 */
class BlokNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var blockedPackages: Set<String> = emptySet()
    private var isBlocking = false
    private var notifBlockEnabled = true

    override fun onListenerConnected() {
        super.onListenerConnected()
        val app = application as? AppBlockerApplication ?: return

        // Watch blocked packages
        scope.launch {
            app.database.blockedAppDao().getAllBlockedPackages().collect { packages ->
                blockedPackages = packages.toSet()
                // Re-check existing notifications when blocked list changes
                if (isBlocking && notifBlockEnabled) {
                    cancelBlockedNotifications()
                }
            }
        }

        // Watch blocking state + notification pref
        scope.launch {
            combine(
                app.blockStateManager.isBlocking,
                app.notificationBlockManager.isEnabled
            ) { blocking, notifEnabled ->
                Pair(blocking, notifEnabled)
            }.collect { (blocking, notifEnabled) ->
                isBlocking = blocking
                notifBlockEnabled = notifEnabled
                if (blocking && notifEnabled) {
                    cancelBlockedNotifications()
                }
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (!isBlocking || !notifBlockEnabled) return
        if (sbn.packageName in blockedPackages) {
            try {
                cancelNotification(sbn.key)
            } catch (_: Exception) {}
        }
    }

    private fun cancelBlockedNotifications() {
        try {
            val active = activeNotifications ?: return
            for (sbn in active) {
                if (sbn.packageName in blockedPackages) {
                    cancelNotification(sbn.key)
                }
            }
        } catch (_: Exception) {}
    }

    override fun onListenerDisconnected() {
        scope.cancel()
        super.onListenerDisconnected()
    }
}
