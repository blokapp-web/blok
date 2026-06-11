package com.appblocker.ui.screens

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appblocker.R
import com.appblocker.data.AppLanguage
import com.appblocker.data.ThemeMode
import com.appblocker.ui.components.GlassBackground
import com.appblocker.ui.components.GlassCard
import com.appblocker.ui.theme.DotMatrix
import com.appblocker.ui.theme.SpaceMono

@Composable
fun SettingsScreen(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    isWriteMode: Boolean,
    onWriteNfc: () -> Unit,
    onCancelWrite: () -> Unit,
    blockNotifications: Boolean,
    onBlockNotificationsChange: (Boolean) -> Unit,
    notificationAccessGranted: Boolean,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    onLanguageChange: (AppLanguage) -> Unit = {}
) {
    val context = LocalContext.current
    var themeExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                stringResource(R.string.settings_header),
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.settings_title),
                fontFamily = DotMatrix,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-1.5).sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Appearance (collapsible) ──
            SectionLabel(stringResource(R.string.settings_appearance))
            Spacer(modifier = Modifier.height(8.dp))

            val themes = listOf(
                ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
                ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
                ThemeMode.SYSTEM to stringResource(R.string.settings_theme_auto)
            )
            val currentThemeDisplay = themes.firstOrNull { it.first == currentTheme }?.second ?: ""
            val primaryC = MaterialTheme.colorScheme.primary
            val onSurfC = MaterialTheme.colorScheme.onSurface

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                Column {
                    // ── Header tab ──
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { themeExpanded = !themeExpanded }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryC.copy(alpha = 0.08f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "//",
                                fontFamily = SpaceMono,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryC
                            )
                        }
                        Spacer(modifier = Modifier.width(13.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_appearance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = onSurfC
                            )
                            Text(
                                currentThemeDisplay,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryC
                            )
                        }
                        Icon(
                            if (themeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // ── Dropdown ──
                    AnimatedVisibility(
                        visible = themeExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            ItemDivider()
                            themes.forEachIndexed { index, (mode, displayName) ->
                                val isSel = currentTheme == mode
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onThemeChange(mode)
                                            themeExpanded = false
                                        }
                                        .background(
                                            if (isSel) primaryC.copy(alpha = 0.08f)
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 20.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        displayName,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) primaryC else onSurfC,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSel) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = primaryC,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                if (index < themes.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                            .height(0.5.dp)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Language (collapsible) ──
            SectionLabel(stringResource(R.string.settings_language))
            Spacer(modifier = Modifier.height(8.dp))

            val languages = listOf(
                AppLanguage.SYSTEM to stringResource(R.string.lang_system),
                AppLanguage.ENGLISH to stringResource(R.string.lang_en),
                AppLanguage.SPANISH to stringResource(R.string.lang_es),
                AppLanguage.FRENCH to stringResource(R.string.lang_fr),
                AppLanguage.GERMAN to stringResource(R.string.lang_de),
                AppLanguage.PORTUGUESE to stringResource(R.string.lang_pt),
                AppLanguage.ITALIAN to stringResource(R.string.lang_it)
            )
            val currentDisplayName = languages.firstOrNull { it.first == currentLanguage }?.second ?: ""
            val primary = MaterialTheme.colorScheme.primary
            val onSurf = MaterialTheme.colorScheme.onSurface

            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                Column {
                    // ── Header tab (always visible) ──
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { languageExpanded = !languageExpanded }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primary.copy(alpha = 0.08f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "A",
                                fontFamily = SpaceMono,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary
                            )
                        }
                        Spacer(modifier = Modifier.width(13.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_language_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = onSurf
                            )
                            Text(
                                currentDisplayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primary
                            )
                        }
                        Icon(
                            if (languageExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // ── Dropdown list (animated) ──
                    AnimatedVisibility(
                        visible = languageExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            ItemDivider()
                            languages.forEachIndexed { index, (lang, displayName) ->
                                val isSel = currentLanguage == lang
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onLanguageChange(lang)
                                            languageExpanded = false
                                        }
                                        .background(
                                            if (isSel) primary.copy(alpha = 0.08f)
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 20.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        displayName,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) primary else onSurf,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSel) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                if (index < languages.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                            .height(0.5.dp)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── NFC ──
            SectionLabel(stringResource(R.string.settings_nfc))
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                Column {
                    SettingsItem(
                        icon = ">>",
                        title = if (isWriteMode) stringResource(R.string.settings_waiting_tag) else stringResource(R.string.settings_write_nfc),
                        sub = if (isWriteMode) stringResource(R.string.settings_waiting_tag_sub) else stringResource(R.string.settings_write_nfc_sub),
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { if (isWriteMode) onCancelWrite() else onWriteNfc() }
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    if (isWriteMode) stringResource(R.string.settings_cancel) else stringResource(R.string.settings_write),
                                    fontFamily = SpaceMono,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D1400),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    )
                    ItemDivider()
                    SettingsItem(
                        icon = "->",
                        title = stringResource(R.string.settings_nfc_system),
                        sub = stringResource(R.string.settings_nfc_system_sub),
                        onClick = {
                            try { context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
                            catch (_: Exception) { context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
                        },
                        trailing = {
                            Text(">", fontFamily = SpaceMono, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Notificaciones ──
            SectionLabel(stringResource(R.string.settings_notifications))
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "!!",
                                fontFamily = SpaceMono,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(13.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_block_notifications),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                stringResource(R.string.settings_block_notifications_sub),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = blockNotifications,
                            onCheckedChange = { onBlockNotificationsChange(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    if (!notificationAccessGranted) {
                        ItemDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.settings_enable_notification_access),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            Text(">", fontFamily = SpaceMono, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── General ──
            SectionLabel(stringResource(R.string.settings_general))
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                SettingsItem(
                    icon = "[]",
                    title = stringResource(R.string.settings_accessibility),
                    sub = stringResource(R.string.settings_accessibility_sub),
                    onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    trailing = {
                        Text(">", fontFamily = SpaceMono, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Device ──
            SectionLabel(stringResource(R.string.settings_device))
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                Column {
                    SettingsItem(icon = "#", title = stringResource(R.string.settings_model), sub = android.os.Build.MODEL)
                    ItemDivider()
                    SettingsItem(icon = "v", title = stringResource(R.string.settings_version), sub = "1.0.0")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontFamily = SpaceMono,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        letterSpacing = 2.sp
    )
}

@Composable
private fun SettingsItem(
    icon: String,
    title: String,
    sub: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                icon,
                fontFamily = SpaceMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        trailing?.invoke()
    }
}

@Composable
private fun ItemDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}
