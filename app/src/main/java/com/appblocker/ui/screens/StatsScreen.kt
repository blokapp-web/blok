package com.appblocker.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appblocker.R
import com.appblocker.data.AppAttemptCount
import com.appblocker.ui.components.GlassBackground
import com.appblocker.ui.components.GlassCard
import com.appblocker.ui.theme.Appear
import com.appblocker.ui.theme.BlokMotion
import com.appblocker.ui.theme.GreenSuccess
import com.appblocker.ui.theme.DotMatrix
import com.appblocker.ui.theme.SpaceMono
import com.appblocker.ui.theme.animatedCount
import com.appblocker.ui.theme.rememberAppearState
import com.appblocker.viewmodel.DayStats
import com.appblocker.viewmodel.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf("week") }
    val onSurf = MaterialTheme.colorScheme.onSurface
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    val weekLabel = stringResource(R.string.stats_week)
    val monthLabel = stringResource(R.string.stats_month)
    val appearState = rememberAppearState()

    GlassBackground {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primary, strokeWidth = 2.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(14.dp)) }

                item {
                    Appear(appearState, 0) {
                    Column {
                    Text(
                        stringResource(R.string.stats_your_progress),
                        fontFamily = SpaceMono,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = dim,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.stats_title),
                        fontFamily = DotMatrix,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurf,
                        letterSpacing = (-1.5).sp
                    )
                    }
                    }
                }

                // Toggle
                item {
                    Appear(appearState, 1) {
                    GlassCard(Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
                        Row(Modifier.padding(4.dp)) {
                            listOf("week" to weekLabel, "month" to monthLabel).forEach { (id, label) ->
                                val isSel = viewMode == id
                                val segBg by animateColorAsState(
                                    if (isSel) onSurf.copy(alpha = 0.08f) else Color.Transparent,
                                    spring(stiffness = Spring.StiffnessMediumLow), label = "segBg"
                                )
                                val segBorder by animateColorAsState(
                                    if (isSel) primary.copy(alpha = 0.3f) else Color.Transparent,
                                    spring(stiffness = Spring.StiffnessMediumLow), label = "segBr"
                                )
                                val segText by animateColorAsState(
                                    if (isSel) primary else dim.copy(alpha = 0.4f),
                                    spring(stiffness = Spring.StiffnessMediumLow), label = "segTx"
                                )
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(segBg)
                                        .border(1.dp, segBorder, RoundedCornerShape(8.dp))
                                        .clickable { viewMode = id }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label.uppercase(),
                                        fontFamily = SpaceMono,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = segText,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                    }
                }

                // Calendar
                item {
                    Appear(appearState, 2) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.animation.Crossfade(
                            targetState = viewMode,
                            animationSpec = tween(350),
                            label = "calendar",
                            modifier = Modifier.animateContentSize(tween(350))
                        ) { mode ->
                            when (mode) {
                                "week" -> WeekView(state.weeklyData, state.selectedDate, onSurf, dim) { viewModel.selectDate(it) }
                                "month" -> MonthView(state.monthData, state.displayedMonth, state.selectedDate, onSurf, dim, { viewModel.selectDate(it) }) { viewModel.changeMonth(it) }
                            }
                        }
                    }
                    }
                }

                // Big stat
                item { Appear(appearState, 3) { BigStatCard(state.todayBlockedMinutes, state.weeklyData, state.selectedDate, onSurf, dim) } }

                // Mini cards
                item {
                    Appear(appearState, 4) {
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        GlassCard(Modifier.weight(1f)) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    stringResource(R.string.stats_streak),
                                    fontFamily = SpaceMono,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dim,
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "${animatedCount(state.streak)}",
                                    fontFamily = SpaceMono,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primary,
                                    letterSpacing = (-2).sp,
                                    lineHeight = 42.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(R.string.stats_consecutive_days), fontSize = 11.sp, color = dim.copy(alpha = 0.5f))
                            }
                        }
                        GlassCard(Modifier.weight(1f)) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    stringResource(R.string.stats_attempts),
                                    fontFamily = SpaceMono,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dim,
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    String.format("%02d", animatedCount(state.todayAttempts)),
                                    fontFamily = SpaceMono,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurf,
                                    letterSpacing = (-2).sp,
                                    lineHeight = 42.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(R.string.stats_today), fontSize = 11.sp, color = dim.copy(alpha = 0.5f))
                            }
                        }
                    }
                    }
                }

                if (state.topApps.isNotEmpty()) {
                    item {
                        Text(
                            if (state.selectedDate == LocalDate.now()) stringResource(R.string.stats_today_by_app) else stringResource(R.string.stats_by_app),
                            fontFamily = SpaceMono,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = dim,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 4.dp).animateItem()
                        )
                    }
                    items(state.topApps, key = { it.packageName }) { app ->
                        Box(Modifier.animateItem()) {
                            AppStatItem(app, state.topApps.first().count, state.todayBlockedMinutes, onSurf, dim)
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun WeekView(data: List<DayStats>, sel: LocalDate, onSurf: Color, dim: Color, onSel: (LocalDate) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Row(Modifier.fillMaxWidth().padding(18.dp)) {
        data.forEach { day ->
            val isSel = day.date == sel
            val isToday = day.date == LocalDate.now()
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase().take(1),
                    fontFamily = SpaceMono,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = dim.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) primary else Color.Transparent)
                        .then(
                            if (isToday && !isSel) Modifier.border(1.dp, primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            else Modifier
                        )
                        .clickable { onSel(day.date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${day.date.dayOfMonth}",
                        fontFamily = SpaceMono,
                        fontSize = 13.sp,
                        fontWeight = if (isSel || isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else onSurf
                    )
                }
                if (day.hadSession) {
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.size(4.dp).clip(CircleShape).background(if (isSel) MaterialTheme.colorScheme.onPrimary else primary))
                }
            }
        }
    }
}

@Composable
private fun MonthView(
    data: List<DayStats>, month: YearMonth, sel: LocalDate,
    onSurf: Color, dim: Color,
    onSel: (LocalDate) -> Unit, onMonth: (YearMonth) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val locale = Locale.getDefault()
    val dayMap = remember(data) { data.associateBy { it.date } }
    val off = remember(month) { month.atDay(1).dayOfWeek.value - 1 }
    val daysInMonth = remember(month) { month.lengthOfMonth() }
    val rows = remember(off, daysInMonth) { (off + daysInMonth + 6) / 7 }

    Column(Modifier.padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(
                "<",
                fontFamily = SpaceMono,
                fontSize = 18.sp,
                color = dim,
                modifier = Modifier.clickable { onMonth(month.minusMonths(1)) }.padding(8.dp)
            )
            Text(
                month.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() } + " ${month.year}",
                fontFamily = SpaceMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = onSurf,
                letterSpacing = 1.sp
            )
            val canNext = !month.plusMonths(1).isAfter(YearMonth.now())
            Text(
                ">",
                fontFamily = SpaceMono,
                fontSize = 18.sp,
                color = if (canNext) dim else dim.copy(alpha = 0.2f),
                modifier = Modifier
                    .then(if (canNext) Modifier.clickable { onMonth(month.plusMonths(1)) } else Modifier)
                    .padding(8.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Day-of-week headers from locale
        val weekDays = remember(locale) {
            DayOfWeek.entries.map { it.getDisplayName(TextStyle.NARROW, locale).uppercase() }
        }
        Row(Modifier.fillMaxWidth()) {
            weekDays.forEach {
                Text(
                    it,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontFamily = SpaceMono,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = dim.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (c in 0..6) {
                    val d = r * 7 + c - off + 1
                    if (d < 1 || d > daysInMonth) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = month.atDay(d)
                        val isSel = date == sel
                        val isToday = date == LocalDate.now()
                        val isFut = date.isAfter(LocalDate.now())
                        val st = dayMap[date]

                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSel -> primary
                                        isToday -> primary.copy(alpha = 0.06f)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(if (!isFut) Modifier.clickable { onSel(date) } else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$d",
                                    fontFamily = SpaceMono,
                                    fontSize = 11.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSel -> MaterialTheme.colorScheme.onPrimary
                                        isFut -> dim.copy(alpha = 0.2f)
                                        else -> onSurf
                                    }
                                )
                                if (st?.hadSession == true && !isSel) {
                                    Box(Modifier.size(3.dp).clip(CircleShape).background(primary))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BigStatCard(min: Long, data: List<DayStats>, sel: LocalDate, onSurf: Color, dim: Color) {
    val primary = MaterialTheme.colorScheme.primary
    val maxM = remember(data) { (data.maxOfOrNull { it.blockedMinutes } ?: 1L).coerceAtLeast(1L) }

    // Day labels from locale
    val locale = Locale.getDefault()
    val dayLabels = remember(locale) {
        DayOfWeek.entries.map { it.getDisplayName(TextStyle.NARROW, locale).uppercase() }
    }

    GlassCard(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(primary.copy(alpha = 0.04f))
                .border(1.dp, primary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
        ) {
            Column(Modifier.padding(22.dp)) {
                Text(
                    stringResource(R.string.stats_time_blocked_today),
                    fontFamily = SpaceMono,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = dim,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(4.dp))

                val h = min / 60; val m = min % 60
                Row(verticalAlignment = Alignment.Bottom) {
                    if (h > 0) {
                        Text("$h", fontFamily = SpaceMono, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = onSurf, letterSpacing = (-3).sp, lineHeight = 56.sp)
                        Text("h ", fontSize = 20.sp, color = dim, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Text("$m", fontFamily = SpaceMono, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = onSurf, letterSpacing = (-3).sp, lineHeight = 56.sp)
                    Text("min", fontSize = 20.sp, color = dim, modifier = Modifier.padding(bottom = 8.dp))
                }

                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.stats_improving), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GreenSuccess)
                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth().height(56.dp), Arrangement.spacedBy(5.dp), Alignment.Bottom) {
                    data.forEach { day ->
                        val isSel = day.date == sel
                        val target = (day.blockedMinutes.toFloat() / maxM).coerceIn(0f, 1f)
                        val anim by animateFloatAsState(target, tween(400), label = "b${day.date.dayOfMonth}")
                        val barH = (anim * 54).coerceAtLeast(if (day.blockedMinutes > 0) 6f else 3f)

                        Box(
                            Modifier
                                .weight(1f)
                                .height(barH.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSel) Brush.verticalGradient(listOf(primary, primary.copy(alpha = 0.7f)))
                                    else Brush.verticalGradient(listOf(onSurf.copy(alpha = 0.08f), onSurf.copy(alpha = 0.08f)))
                                )
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))

                Row(Modifier.fillMaxWidth()) {
                    data.forEachIndexed { i, day ->
                        Text(
                            dayLabels.getOrElse(i) { "" },
                            Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontFamily = SpaceMono,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (day.date == sel) primary else dim.copy(alpha = 0.3f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppStatItem(app: AppAttemptCount, maxC: Int, totalMin: Long, onSurf: Color, dim: Color) {
    val primary = MaterialTheme.colorScheme.primary
    val ctx = LocalContext.current
    var icon by remember(app.packageName) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(app.packageName) {
        withContext(Dispatchers.IO) {
            try {
                val d: Drawable = ctx.packageManager.getApplicationIcon(app.packageName)
                icon = d.toBitmap(48, 48).asImageBitmap()
            } catch (_: Exception) {}
        }
    }

    val targetFraction = remember(app.count, maxC) { if (maxC > 0) app.count.toFloat() / maxC else 0f }
    val fraction by animateFloatAsState(targetFraction, tween(500, easing = BlokMotion.Ease), label = "frac")
    val appMin = remember(totalMin, app.count, maxC) { (totalMin * app.count) / maxC.coerceAtLeast(1) }
    val h = appMin / 60; val m = appMin % 60
    val timeStr = if (h > 0) "${h}h ${m}m" else "${m}m"
    val attemptStr = if (app.count != 1) stringResource(R.string.stats_attempt_plural) else stringResource(R.string.stats_attempt_singular)

    GlassCard(Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Image(icon!!, app.appName, Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)))
            } else {
                Box(Modifier.size(36.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(
                        app.appName, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = onSurf, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(timeStr, fontFamily = SpaceMono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primary)
                        Text("${app.count} $attemptStr", fontSize = 10.sp, color = dim)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.fillMaxWidth().height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(onSurf.copy(alpha = 0.04f))
                ) {
                    Box(
                        Modifier.fillMaxWidth(fraction).height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(primary)
                    )
                }
            }
        }
    }
}
