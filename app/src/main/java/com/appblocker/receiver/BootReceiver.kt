package com.appblocker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.appblocker.AppBlockerApplication

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restaurar el estado de bloqueo guardado en SharedPreferences
            val app = context.applicationContext as AppBlockerApplication
            app.blockStateManager.restore()
        }
    }
}
