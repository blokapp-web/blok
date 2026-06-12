package com.appblocker.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// ── Shared motion layer ──
// One vocabulary for the whole app: same durations, same easing, same springs.
object BlokMotion {
    const val Fast = 160
    const val Base = 260
    const val Counter = 600
    const val StaggerStep = 45
    val Ease = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}

// Tactile press feedback: subtle scale-down while pressed.
// GPU-only (graphicsLayer) — no relayout. Share the InteractionSource with clickable.
@Composable
fun Modifier.pressScale(source: MutableInteractionSource): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.96f else 1f,
        spring(stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

// One-shot entrance: fade + 12dp rise, staggered by index.
// Runs once per composition — never replays while the screen stays alive.
@Composable
fun Appear(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    Appear(state, index, modifier, content)
}

// Variant with hoisted state, for LazyColumn screens: remember the state at
// screen level (rememberAppearState) so scrolling items off and back never replays.
@Composable
fun Appear(
    state: MutableTransitionState<Boolean>,
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val delay = index * BlokMotion.StaggerStep
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = fadeIn(tween(BlokMotion.Base, delayMillis = delay, easing = BlokMotion.Ease)) +
                slideInVertically(
                    tween(BlokMotion.Base, delayMillis = delay, easing = BlokMotion.Ease)
                ) { it / 6 },
        exit = ExitTransition.None
    ) { content() }
}

@Composable
fun rememberAppearState(): MutableTransitionState<Boolean> =
    remember { MutableTransitionState(false).apply { targetState = true } }

// Animated count-up for metrics. Returns the value to render.
@Composable
fun animatedCount(value: Int): Int {
    val anim by animateIntAsState(
        value,
        tween(BlokMotion.Counter, easing = BlokMotion.Ease),
        label = "count"
    )
    return anim
}
