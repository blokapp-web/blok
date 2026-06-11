package com.appblocker.ui.screens

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appblocker.AppBlockerApplication
import com.appblocker.R
import com.appblocker.ui.theme.NeonYellow
import com.appblocker.ui.theme.DotMatrix
import com.appblocker.ui.theme.SpaceMono

class BlockOverlayActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null

    // Reactive state so UI updates when a different blocked app triggers
    private var displayedAppName by mutableStateOf("App")
    private var blockedPackageName by mutableStateOf<String?>(null)
    private var emergencyLeft by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this) { goHome() }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        updateFromIntent(intent)

        setContent {
            BlockOverlayScreen(
                appName = displayedAppName,
                emergencyUsesLeft = emergencyLeft,
                onEmergencyUnlock = { handleEmergencyUnlock() }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Check if it's an NFC intent
        val action = intent.action
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED
        ) {
            handleNfcIntent(intent)
            return
        }

        // Otherwise it's a new blocked app — update displayed info
        updateFromIntent(intent)
    }

    private fun updateFromIntent(intent: Intent) {
        displayedAppName = intent.getStringExtra("app_name") ?: "App"
        blockedPackageName = intent.getStringExtra("package_name")
        val app = application as AppBlockerApplication
        emergencyLeft = app.emergencyUnlockManager.remainingUses.value
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let { adapter ->
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
            adapter.enableForegroundDispatch(this, pending, filters, null)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    private fun handleNfcIntent(intent: Intent) {
        val app = application as AppBlockerApplication
        val nowBlocking = app.blockStateManager.toggle()
        Toast.makeText(
            this,
            if (nowBlocking) getString(R.string.toast_blok_on) else getString(R.string.toast_blok_off),
            Toast.LENGTH_SHORT
        ).show()
        if (!nowBlocking) {
            returnToBlockedApp()
        }
    }

    private fun handleEmergencyUnlock() {
        val app = application as AppBlockerApplication
        if (app.emergencyUnlockManager.useEmergencyUnlock()) {
            app.blockStateManager.toggle()
            Toast.makeText(this, getString(R.string.toast_emergency_remaining, app.emergencyUnlockManager.remainingUses.value), Toast.LENGTH_LONG).show()
            returnToBlockedApp()
        } else {
            Toast.makeText(this, getString(R.string.toast_no_emergency_left), Toast.LENGTH_LONG).show()
        }
    }

    private fun returnToBlockedApp() {
        val pkg = blockedPackageName
        if (pkg != null) {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            }
        }
        finish()
    }

    private fun goHome() {
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        finish()
    }
}

@Composable
private fun BlockOverlayScreen(
    appName: String,
    emergencyUsesLeft: Int,
    onEmergencyUnlock: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "scale"
    )
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "glow"
    )
    val nfcAlpha by pulse.animateFloat(
        initialValue = 0.2f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "nfc"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                        .alpha(glowAlpha)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NeonYellow.copy(alpha = 0.4f),
                                    NeonYellow.copy(alpha = 0f)
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(NeonYellow.copy(alpha = 0.15f))
                        .border(1.dp, NeonYellow.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = NeonYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "BLOCKED",
                fontFamily = DotMatrix,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = appName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.25f)
            )

            Spacer(modifier = Modifier.height(56.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(nfcAlpha)
            ) {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = NeonYellow.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "TAP YOUR NFC\nTO UNLOCK",
                    fontFamily = SpaceMono,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.2f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    letterSpacing = 2.sp
                )
            }

            if (emergencyUsesLeft > 0) {
                Spacer(modifier = Modifier.height(48.dp))
                OutlinedButton(
                    onClick = onEmergencyUnlock,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonYellow)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "EMERGENCY ($emergencyUsesLeft)",
                        fontFamily = SpaceMono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
