package com.appblocker

import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Invisible activity that handles ALL NFC taps when the app is NOT in the foreground.
 * - taskAffinity="" → runs in its own task, never brings BLOK to the front
 * - Theme.Transparent → no visible UI
 * - Toggles block state, shows toast, finishes immediately
 */
class NfcToggleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Toggle regardless of intent action — if we got here, it's an NFC tap
        val app = application as AppBlockerApplication
        val nowBlocking = app.blockStateManager.toggle()
        Toast.makeText(
            this,
            if (nowBlocking) "BLOK activado" else "BLOK desactivado",
            Toast.LENGTH_SHORT
        ).show()

        finishAndRemoveTask()
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
}
