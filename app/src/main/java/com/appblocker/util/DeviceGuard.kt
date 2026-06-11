package com.appblocker.util

import android.os.Build

object DeviceGuard {

    // Samsung Galaxy S25: todos los modelos regionales empiezan por SM-S931
    // Si la app no arranca, ejecuta en ADB:  adb shell getprop ro.product.model
    // y añade el prefijo aquí.
    private val ALLOWED_MODEL_PREFIXES = listOf("SM-S931")

    fun isAuthorizedDevice(): Boolean {
        return ALLOWED_MODEL_PREFIXES.any { Build.MODEL.startsWith(it, ignoreCase = true) }
    }

    fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, SDK ${Build.VERSION.SDK_INT})"
    }
}
