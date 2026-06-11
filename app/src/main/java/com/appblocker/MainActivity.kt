package com.appblocker

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.ComponentName
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appblocker.data.AppLanguage
import com.appblocker.service.AppBlockerAccessibilityService
import com.appblocker.ui.screens.AppSelectionScreen
import com.appblocker.ui.screens.HomeScreen
import com.appblocker.ui.screens.SettingsScreen
import com.appblocker.ui.screens.SpaceEditorScreen
import com.appblocker.ui.screens.SpacesScreen
import com.appblocker.ui.screens.StatsScreen
import com.appblocker.ui.theme.AppBlockerTheme
import com.appblocker.ui.theme.SpaceMono
import com.appblocker.util.DeviceGuard
import com.appblocker.viewmodel.AppSelectionViewModel
import com.appblocker.viewmodel.SpacesViewModel
import com.appblocker.viewmodel.StatsViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var currentScreen by mutableStateOf(Screen.TABS)
    var isWriteMode by mutableStateOf(false)
        private set

    private enum class Screen { TABS, APP_SELECTION, SPACES, SPACE_EDITOR }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Apply saved language
        val app = application as AppBlockerApplication
        applyLanguage(app.languageManager.language.value)

        setContent {
            val themeMode by app.themeManager.themeMode.collectAsStateWithLifecycle()
            AppBlockerTheme(themeMode = themeMode) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (DeviceGuard.isAuthorizedDevice()) AppContent() else UnauthorizedScreen()
                }
            }
        }
    }

    private fun applyLanguage(lang: AppLanguage) {
        val localeList = if (lang == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(lang.code)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private var editingSpaceId by mutableStateOf<Long?>(null)

    @Composable
    private fun AppContent() {
        val app = application as AppBlockerApplication
        val appVm: AppSelectionViewModel = viewModel()
        val statsVm: StatsViewModel = viewModel()
        val spacesVm: SpacesViewModel = viewModel()
        val isBlocking by app.blockStateManager.isBlocking.collectAsStateWithLifecycle()
        val blockedCount by appVm.blockedCount.collectAsStateWithLifecycle()
        val blockedAppNames by appVm.blockedAppNames.collectAsStateWithLifecycle()
        val emergencyUsesLeft by app.emergencyUnlockManager.remainingUses.collectAsStateWithLifecycle()
        val statsState by statsVm.state.collectAsStateWithLifecycle()
        val themeMode by app.themeManager.themeMode.collectAsStateWithLifecycle()
        val accessibilityEnabled by AppBlockerAccessibilityService.isRunning.collectAsStateWithLifecycle()
        val activeSpace by app.spaceManager.activeSpace.collectAsStateWithLifecycle()
        val blockNotifications by app.notificationBlockManager.isEnabled.collectAsStateWithLifecycle()
        val currentLanguage by app.languageManager.language.collectAsStateWithLifecycle()

        // Check if notification listener is enabled
        val notifAccessGranted = remember {
            val cn = ComponentName(this@MainActivity, "com.appblocker.service.BlokNotificationListenerService")
            val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: ""
            enabledListeners.contains(cn.flattenToString())
        }

        val scope = rememberCoroutineScope()
        // Page order: 0=Stats, 1=Home (initial), 2=Settings
        val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

        // ── Back button: go to previous screen, never exit app ──
        BackHandler {
            when (currentScreen) {
                Screen.SPACE_EDITOR -> currentScreen = Screen.SPACES
                Screen.SPACES -> currentScreen = Screen.TABS
                Screen.APP_SELECTION -> currentScreen = Screen.TABS
                Screen.TABS -> {
                    if (pagerState.currentPage != 1) {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                    // On Home page: do nothing (don't exit)
                }
            }
        }

        when (currentScreen) {
            Screen.TABS -> {
                Box(Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                        beyondViewportPageCount = 1
                    ) { page ->
                        when (page) {
                            0 -> StatsScreen(viewModel = statsVm)
                            1 -> HomeScreen(
                                isBlocking = isBlocking,
                                blockedCount = blockedCount,
                                blockedMinutes = statsState.todayBlockedMinutes,
                                todayAttempts = statsState.todayAttempts,
                                blockedAppNames = blockedAppNames,
                                accessibilityEnabled = accessibilityEnabled,
                                emergencyUsesLeft = emergencyUsesLeft,
                                activeSpaceName = activeSpace?.name,
                                activeSpaceIcon = activeSpace?.iconName,
                                onEditApps = { currentScreen = Screen.APP_SELECTION },
                                onOpenSpaces = { currentScreen = Screen.SPACES },
                                onEmergencyUnlock = {
                                    if (app.emergencyUnlockManager.useEmergencyUnlock()) {
                                        app.blockStateManager.toggle()
                                        Toast.makeText(this@MainActivity, getString(R.string.toast_emergency_unlock), Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                            2 -> SettingsScreen(
                                currentTheme = themeMode,
                                onThemeChange = { app.themeManager.setTheme(it) },
                                isWriteMode = isWriteMode,
                                onWriteNfc = { isWriteMode = true },
                                onCancelWrite = { isWriteMode = false },
                                blockNotifications = blockNotifications,
                                onBlockNotificationsChange = { app.notificationBlockManager.setEnabled(it) },
                                notificationAccessGranted = notifAccessGranted,
                                currentLanguage = currentLanguage,
                                onLanguageChange = { lang ->
                                    app.languageManager.setLanguage(lang)
                                    applyLanguage(lang)
                                }
                            )
                        }
                    }

                    IndustrialNav(
                        selectedTab = pagerState.currentPage,
                        onTabSelected = { tab ->
                            scope.launch { pagerState.animateScrollToPage(tab) }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                    )
                }
            }
            Screen.APP_SELECTION -> {
                AppSelectionScreen(viewModel = appVm, showBackButton = true, onConfirm = { currentScreen = Screen.TABS })
            }
            Screen.SPACES -> {
                SpacesScreen(
                    viewModel = spacesVm,
                    onBack = { currentScreen = Screen.TABS },
                    onCreateSpace = {
                        spacesVm.startNewSpace()
                        editingSpaceId = null
                        currentScreen = Screen.SPACE_EDITOR
                    },
                    onEditSpace = { spaceId ->
                        spacesVm.startEditSpace(spaceId)
                        editingSpaceId = spaceId
                        currentScreen = Screen.SPACE_EDITOR
                    }
                )
            }
            Screen.SPACE_EDITOR -> {
                SpaceEditorScreen(
                    viewModel = spacesVm,
                    isEditing = editingSpaceId != null,
                    onDone = { currentScreen = Screen.SPACES }
                )
            }
        }
    }

    // --- NFC (foreground dispatch only — NfcToggleActivity handles external NFC) ---
    override fun onResume() {
        super.onResume()
        nfcAdapter?.let { adapter ->
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            adapter.enableForegroundDispatch(this, pending, arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)), null)
        }
    }

    override fun onPause() { super.onPause(); nfcAdapter?.disableForegroundDispatch(this) }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action ?: return
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED
        ) {
            if (isWriteMode) {
                @Suppress("DEPRECATION")
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
                writeTagAndFinish(tag)
            } else {
                toggleBlockState()
            }
        }
    }

    private fun toggleBlockState() {
        val app = application as AppBlockerApplication
        val nowBlocking = app.blockStateManager.toggle()
        Toast.makeText(this, if (nowBlocking) getString(R.string.toast_blok_on) else getString(R.string.toast_blok_off), Toast.LENGTH_SHORT).show()
    }

    private fun writeTagAndFinish(tag: Tag) {
        val downloadUrl = APK_DOWNLOAD_URL
        val records = mutableListOf<NdefRecord>()
        if (downloadUrl.isNotBlank()) records.add(NdefRecord.createUri(downloadUrl))
        records.add(NdefRecord.createApplicationRecord(packageName))
        val message = NdefMessage(records.toTypedArray())
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) { Toast.makeText(this, getString(R.string.toast_read_only), Toast.LENGTH_LONG).show(); ndef.close(); return }
                if (ndef.maxSize < message.toByteArray().size) { Toast.makeText(this, getString(R.string.toast_no_space), Toast.LENGTH_LONG).show(); ndef.close(); return }
                ndef.writeNdefMessage(message); ndef.close()
                Toast.makeText(this, getString(R.string.toast_tag_written), Toast.LENGTH_SHORT).show()
            } else {
                val fmt = NdefFormatable.get(tag)
                if (fmt != null) { fmt.connect(); fmt.format(message); fmt.close(); Toast.makeText(this, getString(R.string.toast_tag_written), Toast.LENGTH_SHORT).show() }
                else Toast.makeText(this, getString(R.string.toast_not_ndef), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) { Toast.makeText(this, getString(R.string.toast_error, e.message ?: ""), Toast.LENGTH_LONG).show() }
        isWriteMode = false
    }

    companion object {
        const val APK_DOWNLOAD_URL = ""
    }
}

// ── Nav: STATS (page0) | HOME (page1, center) | SETTINGS (page2) ──
@Composable
private fun IndustrialNav(selectedTab: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val subtleBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val subtleBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // STATS — left
        SideNavItem(
            filled = Icons.Filled.BarChart,
            outlined = Icons.Outlined.BarChart,
            label = stringResource(R.string.nav_stats),
            selected = selectedTab == 0
        ) { onTabSelected(0) }

        // HOME — center, bigger
        val isCenterSel = selectedTab == 1
        val centerBg by animateColorAsState(
            if (isCenterSel) primary else subtleBg,
            spring(stiffness = Spring.StiffnessMediumLow), label = "cbg"
        )
        val centerTint by animateColorAsState(
            if (isCenterSel) onPrimary else muted,
            spring(stiffness = Spring.StiffnessMediumLow), label = "ct"
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(centerBg)
                .border(
                    1.dp,
                    if (isCenterSel) primary else subtleBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable(remember { MutableInteractionSource() }, null) { onTabSelected(1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isCenterSel) Icons.Filled.GridView else Icons.Outlined.GridView,
                stringResource(R.string.nav_home),
                Modifier.size(24.dp),
                tint = centerTint
            )
        }

        // SETTINGS — right
        SideNavItem(
            filled = Icons.Filled.Settings,
            outlined = Icons.Outlined.Settings,
            label = stringResource(R.string.nav_settings),
            selected = selectedTab == 2
        ) { onTabSelected(2) }
    }
}

@Composable
private fun SideNavItem(filled: ImageVector, outlined: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val tint by animateColorAsState(
        if (selected) primary else muted,
        spring(stiffness = Spring.StiffnessMediumLow), label = "t"
    )
    Column(
        modifier = Modifier
            .clickable(remember { MutableInteractionSource() }, null, onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            if (selected) filled else outlined,
            label,
            Modifier.size(22.dp),
            tint = tint
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontFamily = SpaceMono,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun UnauthorizedScreen() {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.unauthorized_title),
                fontFamily = SpaceMono,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                DeviceGuard.getDeviceInfo(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
