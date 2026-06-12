package com.appblocker.util

import android.os.Build

object DeviceGuard {

    fun isAuthorizedDevice(): Boolean = true

    fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, SDK ${Build.VERSION.SDK_INT})"
    }
}
