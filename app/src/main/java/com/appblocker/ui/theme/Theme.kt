package com.appblocker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.appblocker.data.ThemeMode

// ── Dark scheme (default) ──
private val DarkScheme = darkColorScheme(
    primary            = NeonYellow,
    onPrimary          = BlackBg,
    primaryContainer   = NeonYellowDim,
    onPrimaryContainer = NeonYellow,
    secondary          = NeonYellow,
    onSecondary        = BlackBg,
    tertiary           = NeonYellow,
    onTertiary         = BlackBg,
    background         = BlackBg,
    onBackground       = TextPrimary,
    surface            = BlackBg,
    onSurface          = TextPrimary,
    surfaceVariant     = CardBg,
    onSurfaceVariant   = TextSecondary,
    outline            = Color(0xFF222222),
    outlineVariant     = Color(0xFF1A1A1A),
    error              = RedSoft,
    onError            = Color.White,
    surfaceContainerLowest  = DarkSurface,
    surfaceContainerLow     = DarkSurface,
    surfaceContainer        = CardBg,
    surfaceContainerHigh    = CardBg,
    surfaceContainerHighest = DarkSurface2,
)

// ── Light scheme — warm amber + wisteria + cinnabar palette ──
val AmberGlow     = Color(0xFFFF9F1C)   // Primary accent — warm amber-orange
val AmberDeep     = Color(0xFFE88A00)   // Slightly deeper for pressed/container
val WisteriaBlue  = Color(0xFF8B95C9)   // Secondary — soft purple-blue
val Cinnabar      = Color(0xFFDC493A)   // Error / destructive — warm red

private val LightScheme = lightColorScheme(
    primary            = AmberGlow,
    onPrimary          = Color(0xFF1A1000),         // Very dark brown on amber
    primaryContainer   = Color(0xFFFFE8C8),         // Soft warm peach
    onPrimaryContainer = Color(0xFF4A2800),
    secondary          = WisteriaBlue,
    onSecondary        = Color.White,
    tertiary           = AmberDeep,
    onTertiary         = Color(0xFF1A1000),
    background         = LightBg,
    onBackground       = LightTextPrimary,
    surface            = LightSurface,
    onSurface          = LightTextPrimary,
    surfaceVariant     = LightSurface2,
    onSurfaceVariant   = LightTextSecondary,
    outline            = Color(0xFFD0D0D5),
    outlineVariant     = Color(0xFFE0E0E4),
    error              = Cinnabar,
    onError            = Color.White,
    surfaceContainerLowest  = Color.White,
    surfaceContainerLow     = Color(0xFFFAFAFC),
    surfaceContainer        = LightSurface2,
    surfaceContainerHigh    = Color(0xFFF0F0F4),
    surfaceContainerHighest = Color(0xFFE8E8EC),
)

@Composable
fun AppBlockerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (isDark) DarkScheme else LightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = if (isDark) BlackBg.toArgb() else LightBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
