package com.appblocker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appblocker.R
import com.appblocker.data.SpaceWithApps
import com.appblocker.ui.components.GlassBackground
import com.appblocker.ui.components.GlassCard
import com.appblocker.ui.theme.BlokMotion
import com.appblocker.ui.theme.DotMatrix
import com.appblocker.ui.theme.SpaceMono
import com.appblocker.ui.theme.pressScale
import com.appblocker.ui.util.getSpaceIcon
import com.appblocker.viewmodel.SpacesViewModel

@Composable
fun SpacesScreen(
    viewModel: SpacesViewModel,
    onBack: () -> Unit,
    onCreateSpace: () -> Unit,
    onEditSpace: (Long) -> Unit
) {
    val spaces by viewModel.spaces.collectAsStateWithLifecycle()
    val activeSpaceId by viewModel.spaceManager.activeSpaceId.collectAsStateWithLifecycle()
    val primary = MaterialTheme.colorScheme.primary
    val onSurf = MaterialTheme.colorScheme.onSurface

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back),
                        tint = onSurf
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.spaces_title),
                        fontFamily = DotMatrix,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurf,
                        letterSpacing = 3.sp
                    )
                    Text(
                        "· · · · · · · · · · ·",
                        fontFamily = SpaceMono,
                        fontSize = 10.sp,
                        color = primary.copy(alpha = 0.5f),
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Spaces List ──
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(spaces, key = { it.space.id }) { spaceWithApps ->
                    Box(Modifier.animateItem()) {
                        SpaceCard(
                            spaceWithApps = spaceWithApps,
                            isActive = spaceWithApps.space.id == activeSpaceId,
                            onActivate = {
                                if (spaceWithApps.space.id == activeSpaceId) {
                                    viewModel.deactivateSpace()
                                } else {
                                    viewModel.activateSpace(spaceWithApps.space.id)
                                }
                            },
                            onEdit = { onEditSpace(spaceWithApps.space.id) },
                            onDelete = { viewModel.deleteSpace(spaceWithApps.space.id) }
                        )
                    }
                }

                // ── Create new space button ──
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    val createSource = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .animateItem()
                            .pressScale(createSource)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(interactionSource = createSource, indication = null, onClick = onCreateSpace)
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(primary.copy(alpha = 0.15f))
                                .border(1.dp, primary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            stringResource(R.string.spaces_create),
                            fontFamily = SpaceMono,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primary,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun SpaceCard(
    spaceWithApps: SpaceWithApps,
    isActive: Boolean,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val space = spaceWithApps.space
    val iconEntry = getSpaceIcon(space.iconName)
    val primary = MaterialTheme.colorScheme.primary
    val onSurf = MaterialTheme.colorScheme.onSurface
    val surfVar = MaterialTheme.colorScheme.surfaceVariant
    val borderColor by animateColorAsState(
        if (isActive) primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        spring(stiffness = Spring.StiffnessMediumLow), label = "bc"
    )
    val bgColor by animateColorAsState(
        if (isActive) primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainer,
        spring(stiffness = Spring.StiffnessMediumLow), label = "bgc"
    )
    val iconBg by animateColorAsState(
        if (isActive) primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        spring(stiffness = Spring.StiffnessMediumLow), label = "ibg"
    )
    val iconTint by animateColorAsState(
        if (isActive) primary else onSurf.copy(alpha = 0.5f),
        spring(stiffness = Spring.StiffnessMediumLow), label = "itn"
    )
    val cardSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .pressScale(cardSource)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(interactionSource = cardSource, indication = null, onClick = onActivate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) (iconEntry?.filled ?: Icons.Filled.Check)
                    else (iconEntry?.outlined ?: Icons.Filled.Check),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name & info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    space.name.uppercase(),
                    fontFamily = SpaceMono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) primary else onSurf,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    String.format(stringResource(R.string.spaces_apps_count), spaceWithApps.apps.size),
                    fontSize = 12.sp,
                    color = onSurf.copy(alpha = 0.4f)
                )
                if (spaceWithApps.apps.isNotEmpty()) {
                    Text(
                        spaceWithApps.apps.take(3).joinToString(" · ") { it.appName } +
                                if (spaceWithApps.apps.size > 3) " ..." else "",
                        fontSize = 11.sp,
                        color = onSurf.copy(alpha = 0.25f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Active indicator — pops in with a gentle spring
            AnimatedVisibility(
                visible = isActive,
                enter = scaleIn(
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    initialScale = 0.5f
                ) + fadeIn(tween(BlokMotion.Fast)),
                exit = scaleOut(tween(BlokMotion.Fast), targetScale = 0.5f) + fadeOut(tween(BlokMotion.Fast))
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Edit & delete
            Column {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.spaces_edit_btn),
                        tint = onSurf.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.spaces_delete_btn),
                        tint = onSurf.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
