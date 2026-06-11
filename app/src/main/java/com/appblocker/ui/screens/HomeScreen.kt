package com.appblocker.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.appblocker.R
import com.appblocker.ui.components.GlassBackground
import com.appblocker.ui.components.GlassCard
import com.appblocker.ui.theme.RedSoft
import com.appblocker.ui.theme.SpaceMono
import com.appblocker.ui.util.getSpaceIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    isBlocking: Boolean,
    blockedCount: Int,
    blockedMinutes: Long,
    todayAttempts: Int,
    blockedAppNames: List<String>,
    accessibilityEnabled: Boolean,
    emergencyUsesLeft: Int,
    activeSpaceName: String?,
    activeSpaceIcon: String?,
    onEditApps: () -> Unit,
    onOpenSpaces: () -> Unit,
    onEmergencyUnlock: () -> Unit
) {
    val context = LocalContext.current
    val onSurf = MaterialTheme.colorScheme.onSurface
    val dim = MaterialTheme.colorScheme.onSurfaceVariant

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── BLOK LOGO (PNG) — tinted for theme ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.blok_logo_white_img),
                    contentDescription = "BLOK",
                    modifier = Modifier.height(28.dp),
                    contentScale = ContentScale.Inside,
                    colorFilter = ColorFilter.tint(onSurf)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!accessibilityEnabled) {
                AlertPill(context)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── CIRCLE HERO ──
            CircleHero(isBlocking = isBlocking)

            Spacer(modifier = Modifier.height(16.dp))

            // ── ACTIVE SPACE CHIP ──
            val primary = MaterialTheme.colorScheme.primary
            val onPrimary = MaterialTheme.colorScheme.onPrimary
            if (activeSpaceName != null) {
                val iconEntry = activeSpaceIcon?.let { getSpaceIcon(it) }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(primary)
                        .clickable(onClick = onOpenSpaces)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (iconEntry != null) {
                        Icon(
                            iconEntry.filled,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        activeSpaceName.uppercase(),
                        fontFamily = SpaceMono,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = onPrimary,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                // No space active — visible primary-outlined button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(primary.copy(alpha = 0.08f))
                        .border(1.5.dp, primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .clickable(onClick = onOpenSpaces)
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "+",
                        fontFamily = SpaceMono,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.home_space),
                        fontFamily = SpaceMono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary.copy(alpha = 0.7f),
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── STATS ROW ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.home_time),
                            fontFamily = SpaceMono,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = dim,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val h = blockedMinutes / 60
                        val m = blockedMinutes % 60
                        Text(
                            "${h}H  ${String.format("%02d", m)}M",
                            fontFamily = SpaceMono,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurf,
                            letterSpacing = 1.sp
                        )
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.home_attempts),
                            fontFamily = SpaceMono,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = dim,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            String.format("%04d", todayAttempts),
                            fontFamily = SpaceMono,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurf,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── APPS BLOQUEADAS (chip style) ──
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        stringResource(R.string.home_blocked_apps),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurf
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    val dots = listOf(
                        Color(0xFFFF6B5B), Color(0xFFB080FF), Color(0xFF69D1FF),
                        MaterialTheme.colorScheme.primary, Color(0xFFFFAA50), Color(0xFF50E0B0)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        blockedAppNames.take(6).forEachIndexed { i, name ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dots[i % dots.size]))
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = onSurf.copy(alpha = 0.8f))
                            }
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .clickable(onClick = onEditApps)
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.home_add), fontSize = 13.sp, color = dim)
                        }
                    }
                }
            }

            if (isBlocking && emergencyUsesLeft > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onEmergencyUnlock,
                    colors = ButtonDefaults.textButtonColors(contentColor = RedSoft.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Warning, null, Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        String.format(stringResource(R.string.home_emergency), emergencyUsesLeft),
                        fontFamily = SpaceMono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Circle Hero — dramatic when blocking ──
@Composable
private fun CircleHero(isBlocking: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    val ringIdle = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val ringIdleMid = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val ringIdleInner = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
    val lockBgIdle = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val lockBorderIdle = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val lockTintIdle = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val pulse = rememberInfiniteTransition(label = "hero")

    val glowScale by pulse.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isBlocking) 1.2f else 0.8f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "gs"
    )
    val glowAlpha by pulse.animateFloat(
        initialValue = if (isBlocking) 0.1f else 0f,
        targetValue = if (isBlocking) 0.45f else 0f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "ga"
    )
    val ringPulse by pulse.animateFloat(
        initialValue = if (isBlocking) 0.08f else 0.03f,
        targetValue = if (isBlocking) 0.25f else 0.03f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "rp"
    )
    val iconScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (isBlocking) 1.1f else 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "is"
    )

    Box(
        modifier = Modifier
            .size(280.dp)
            .drawBehind {
                val center = Offset(size.width / 2, size.height / 2)

                if (isBlocking) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = ringPulse * 0.4f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 1.8f
                        ),
                        radius = size.minDimension / 1.8f,
                        center = center
                    )
                }

                drawCircle(
                    color = if (isBlocking) accent.copy(alpha = ringPulse)
                    else ringIdle,
                    radius = size.minDimension / 2,
                    center = center,
                    style = Stroke(width = if (isBlocking) 1.5.dp.toPx() else 1.dp.toPx())
                )
                drawCircle(
                    color = if (isBlocking) accent.copy(alpha = ringPulse * 0.6f)
                    else ringIdleMid,
                    radius = size.minDimension / 2.6f,
                    center = center,
                    style = Stroke(width = if (isBlocking) 1.dp.toPx() else 0.5.dp.toPx())
                )
                drawCircle(
                    color = if (isBlocking) accent.copy(alpha = ringPulse * 0.4f)
                    else ringIdleInner,
                    radius = size.minDimension / 4f,
                    center = center,
                    style = Stroke(width = 0.5.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isBlocking) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(glowScale)
                    .alpha(glowAlpha)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.6f),
                                accent.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .size(if (isBlocking) 72.dp else 64.dp)
                .scale(iconScale)
                .clip(RoundedCornerShape(if (isBlocking) 20.dp else 18.dp))
                .background(
                    if (isBlocking) accent.copy(alpha = 0.2f)
                    else lockBgIdle
                )
                .border(
                    width = if (isBlocking) 2.dp else 1.dp,
                    color = if (isBlocking) accent.copy(alpha = 0.6f) else lockBorderIdle,
                    shape = RoundedCornerShape(if (isBlocking) 20.dp else 18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isBlocking) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(if (isBlocking) 32.dp else 28.dp),
                tint = if (isBlocking) accent else lockTintIdle
            )
        }
    }
}

@Composable
private fun AppIconSmall(appName: String, context: Context) {
    var icon by remember(appName) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(appName) {
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val installed = pm.getInstalledApplications(0)
                val match = installed.firstOrNull {
                    pm.getApplicationLabel(it).toString().equals(appName, ignoreCase = true)
                }
                if (match != null) {
                    val d: Drawable = pm.getApplicationIcon(match.packageName)
                    icon = d.toBitmap(48, 48).asImageBitmap()
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(
                icon!!,
                contentDescription = appName,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
            )
        } else {
            Text(
                appName.take(1).uppercase(),
                fontFamily = SpaceMono,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun AlertPill(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RedSoft.copy(alpha = 0.08f))
            .border(1.dp, RedSoft.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, null, Modifier.size(16.dp), tint = RedSoft)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.alert_accessibility), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RedSoft)
        }
        Button(
            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RedSoft, contentColor = Color.White)
        ) {
            Text("OK", fontFamily = SpaceMono, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        }
    }
}
