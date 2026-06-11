package com.appblocker.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appblocker.R
import com.appblocker.model.InstalledApp
import com.appblocker.ui.components.GlassBackground
import com.appblocker.ui.components.GlassCard
import com.appblocker.ui.theme.DotMatrix
import com.appblocker.ui.theme.SpaceMono
import com.appblocker.ui.util.getSpaceIcon
import com.appblocker.ui.util.spaceIconCatalog
import com.appblocker.viewmodel.SpacesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Multi-step space editor: 0=Name+Icon, 1=Apps
 */
@Composable
fun SpaceEditorScreen(
    viewModel: SpacesViewModel,
    isEditing: Boolean,
    onDone: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    GlassBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            when (step) {
                0 -> SpaceNameIconStep(
                    viewModel = viewModel,
                    isEditing = isEditing,
                    onBack = onDone,
                    onNext = { step = 1 }
                )
                1 -> SpaceAppsStep(
                    viewModel = viewModel,
                    isEditing = isEditing,
                    onBack = { step = 0 },
                    onDone = {
                        viewModel.saveSpace { onDone() }
                    }
                )
            }
        }
    }
}

// ── Step 1: Name + Icon ──
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpaceNameIconStep(
    viewModel: SpacesViewModel,
    isEditing: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val name by viewModel.editorName.collectAsStateWithLifecycle()
    val selectedIcon by viewModel.editorIcon.collectAsStateWithLifecycle()
    val primary = MaterialTheme.colorScheme.primary
    val onSurf = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nav_back), tint = onSurf)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                if (isEditing) stringResource(R.string.spaces_edit) else stringResource(R.string.spaces_new),
                fontFamily = DotMatrix,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = onSurf,
                letterSpacing = 2.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── Preview ──
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val previewIcon = getSpaceIcon(selectedIcon)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(primary.copy(alpha = 0.15f))
                            .border(2.dp, primary.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            previewIcon?.filled ?: Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        name.ifBlank { "..." }.uppercase(),
                        fontFamily = SpaceMono,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (name.isBlank()) onSurf.copy(alpha = 0.2f) else primary,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Name input ──
            Text(
                stringResource(R.string.spaces_name),
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = onSurf.copy(alpha = 0.4f),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = name,
                onValueChange = { viewModel.setEditorName(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.spaces_name_hint),
                        color = onSurf.copy(alpha = 0.15f)
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = onSurf.copy(alpha = 0.05f),
                    unfocusedContainerColor = onSurf.copy(alpha = 0.05f),
                    focusedIndicatorColor = primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = primary,
                    focusedTextColor = onSurf,
                    unfocusedTextColor = onSurf
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Icon picker ──
            Text(
                stringResource(R.string.spaces_icon),
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = onSurf.copy(alpha = 0.4f),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spaceIconCatalog.forEach { entry ->
                    val isSelected = entry.key == selectedIcon
                    val bg by animateColorAsState(
                        if (isSelected) primary.copy(alpha = 0.2f)
                        else onSurf.copy(alpha = 0.04f),
                        spring(stiffness = Spring.StiffnessMediumLow), label = "ibg"
                    )
                    val brd by animateColorAsState(
                        if (isSelected) primary
                        else Color.Transparent,
                        spring(stiffness = Spring.StiffnessMediumLow), label = "ibrd"
                    )
                    val tint by animateColorAsState(
                        if (isSelected) primary
                        else onSurf.copy(alpha = 0.35f),
                        spring(stiffness = Spring.StiffnessMediumLow), label = "it"
                    )

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .border(1.dp, brd, RoundedCornerShape(12.dp))
                            .clickable { viewModel.setEditorIcon(entry.key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isSelected) entry.filled else entry.outlined,
                            contentDescription = entry.label,
                            modifier = Modifier.size(22.dp),
                            tint = tint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Next button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            ExtendedFloatingActionButton(
                onClick = onNext,
                shape = RoundedCornerShape(14.dp),
                containerColor = if (name.isBlank()) onSurf.copy(alpha = 0.1f) else primary,
                contentColor = if (name.isBlank()) onSurf.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Text(
                    stringResource(R.string.spaces_apps_next),
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ── Step 2: App selection ──
@Composable
private fun SpaceAppsStep(
    viewModel: SpacesViewModel,
    isEditing: Boolean,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val apps by viewModel.installedApps.collectAsStateWithLifecycle()
    val selectedPackages by viewModel.editorSelectedPackages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val name by viewModel.editorName.collectAsStateWithLifecycle()
    val primary = MaterialTheme.colorScheme.primary
    val onSurf = MaterialTheme.colorScheme.onSurface

    val filteredApps = remember(apps, searchQuery, selectedPackages) {
        apps
            .map { it.copy(isBlocked = it.packageName in selectedPackages) }
            .filter { searchQuery.isBlank() || it.appName.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nav_back), tint = onSurf)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name.uppercase(),
                    fontFamily = SpaceMono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary,
                    letterSpacing = 1.sp
                )
                Text(
                    String.format(stringResource(R.string.spaces_apps_selected), selectedPackages.size),
                    fontSize = 12.sp,
                    color = onSurf.copy(alpha = 0.4f)
                )
            }
        }

        // Search
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = {
                Text(
                    stringResource(R.string.app_select_search),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.app_select_clear))
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = onSurf.copy(alpha = 0.05f),
                unfocusedContainerColor = onSurf.copy(alpha = 0.05f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = primary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primary, strokeWidth = 2.dp)
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        SpaceAppItem(
                            app = app,
                            onToggle = { viewModel.toggleAppInEditor(app) }
                        )
                    }
                }

                // Save FAB
                ExtendedFloatingActionButton(
                    onClick = onDone,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                    text = {
                        Text(
                            if (isEditing) stringResource(R.string.spaces_save) else stringResource(R.string.spaces_create_btn),
                            fontFamily = SpaceMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    containerColor = primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
                )
            }
        }
    }
}

@Composable
private fun SpaceAppItem(
    app: InstalledApp,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    var icon by remember { mutableStateOf<ImageBitmap?>(null) }
    val primary = MaterialTheme.colorScheme.primary

    LaunchedEffect(app.packageName) {
        withContext(Dispatchers.IO) {
            try {
                val drawable: Drawable = context.packageManager.getApplicationIcon(app.packageName)
                icon = drawable.toBitmap(width = 48, height = 48).asImageBitmap()
            } catch (_: Exception) {}
        }
    }

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Image(
                        bitmap = icon!!,
                        contentDescription = app.appName,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = app.appName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    uncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            )
        }
    }
}
