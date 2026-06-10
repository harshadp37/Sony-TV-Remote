package com.smartir.remote.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.smartir.remote.adb.TvApp
import com.smartir.remote.adb.TvApps
import com.smartir.remote.data.toTvApp
import com.smartir.remote.ui.viewmodel.RemoteViewModel

private const val MAX_SELECTED = 6
private val ITEM_HEIGHT_DP = 56

// Preset colors for custom apps
private val COLOR_PRESETS = listOf(
    0xFFE53935L, // Red
    0xFFFF9800L, // Orange
    0xFF4CAF50L, // Green
    0xFF2196F3L, // Blue
    0xFF6C3FC5L, // Purple
    0xFF00BCD4L, // Cyan
    0xFFFF5722L, // Deep Orange
    0xFF607D8BL, // Blue Grey
    0xFF8BC34AL, // Light Green
    0xFFE91E63L  // Pink
)

@Composable
fun AppShortcutsScreen(
    viewModel: RemoteViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedApps by viewModel.selectedApps.collectAsState()
    val customApps by viewModel.customApps.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Build available apps (catalog + custom, excluding selected)
    val selectedIds = selectedApps.map { it.id }.toSet()
    val customTvApps = customApps.map { it.toTvApp() }
    val allApps = TvApps.CATALOG + customTvApps.filter { custom ->
        TvApps.CATALOG.none { it.id == custom.id }
    }
    val availableApps = allApps.filter { it.id !in selectedIds }
    val isMaxReached = selectedApps.size >= MAX_SELECTED

    // Drag state
    val dragList = remember(selectedApps) { mutableStateListOf(*selectedApps.toTypedArray()) }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // Sync dragList when selectedApps changes externally
    if (draggedIndex == -1 && dragList.toList() != selectedApps) {
        dragList.clear()
        dragList.addAll(selectedApps)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF111118))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF9898A8)
                )
            }
            Text(
                text = "App Shortcuts",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFE8E8F0)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Selected section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF9898A8)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${dragList.size}/$MAX_SELECTED)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6C6C7C)
                )
            }

            // Selected apps — draggable
            val density = LocalDensity.current
            val itemHeightPx = with(density) { ITEM_HEIGHT_DP.dp.toPx() }

            if (dragList.isEmpty()) {
                Text(
                    text = "No apps selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6C6C7C),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
                )
            }

            dragList.forEachIndexed { index, app ->
                val isDragging = draggedIndex == index
                val animatedOffset by animateFloatAsState(
                    targetValue = if (isDragging) dragOffset else 0f,
                    label = "dragOffset"
                )

                SelectedAppRow(
                    app = app,
                    isDragging = isDragging,
                    offsetY = if (isDragging) dragOffset else animatedOffset,
                    onRemove = {
                        val updated = dragList.toMutableList()
                        updated.removeAt(index)
                        dragList.clear()
                        dragList.addAll(updated)
                        viewModel.saveSelectedAppIds(updated.map { it.id })
                    },
                    dragModifier = Modifier.pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedIndex = index
                                dragOffset = 0f
                            },
                            onDrag = { change, offset ->
                                change.consume()
                                dragOffset += offset.y

                                // Calculate target from current dragged position, not the stale closure index
                                val targetIndex = (draggedIndex + (dragOffset / itemHeightPx).toInt())
                                    .coerceIn(0, dragList.size - 1)

                                if (targetIndex != draggedIndex) {
                                    val item = dragList.removeAt(draggedIndex)
                                    dragList.add(targetIndex, item)
                                    dragOffset += (draggedIndex - targetIndex) * itemHeightPx
                                    draggedIndex = targetIndex
                                }
                            },
                            onDragEnd = {
                                draggedIndex = -1
                                dragOffset = 0f
                                viewModel.saveSelectedAppIds(dragList.map { it.id })
                            },
                            onDragCancel = {
                                draggedIndex = -1
                                dragOffset = 0f
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = Color(0xFF2A2A40))

            Spacer(modifier = Modifier.height(16.dp))

            // Available section header
            Text(
                text = "Available",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF9898A8),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            if (isMaxReached) {
                Text(
                    text = "Maximum $MAX_SELECTED apps selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6C6C7C),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Available apps
            availableApps.forEach { app ->
                AvailableAppRow(
                    app = app,
                    enabled = !isMaxReached,
                    onAdd = {
                        val updated = dragList.toMutableList()
                        updated.add(app)
                        dragList.clear()
                        dragList.addAll(updated)
                        viewModel.saveSelectedAppIds(updated.map { it.id })
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add Custom App button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showAddDialog = true }
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Add Custom App",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Add Custom App dialog
    if (showAddDialog) {
        AddCustomAppDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, packageName, colorHex ->
                viewModel.addCustomApp(name, packageName, colorHex)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SelectedAppRow(
    app: TvApp,
    isDragging: Boolean,
    offsetY: Float,
    onRemove: () -> Unit,
    dragModifier: Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ITEM_HEIGHT_DP.dp)
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = if (isDragging) offsetY else 0f
                shadowElevation = if (isDragging) 8f else 0f
            }
            .then(
                if (isDragging) Modifier.background(
                    Color(0xFF1E1E30),
                    RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Drag handle
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Drag to reorder",
            tint = Color(0xFF6C6C7C),
            modifier = dragModifier.size(24.dp)
        )

        // Color dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(app.color)
        )

        // App name
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFE8E8F0),
            modifier = Modifier.weight(1f)
        )

        // Remove button
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove ${app.name}",
                tint = Color(0xFF9898A8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AvailableAppRow(
    app: TvApp,
    enabled: Boolean,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ITEM_HEIGHT_DP.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (enabled) Modifier.clickable(onClick = onAdd)
                else Modifier
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Color dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (enabled) app.color else Color(0xFF3A3A3A))
        )

        // App name
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) Color(0xFFE8E8F0) else Color(0xFF6C6C7C),
            modifier = Modifier.weight(1f)
        )

        // Add button
        IconButton(
            onClick = onAdd,
            enabled = enabled,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add ${app.name}",
                tint = if (enabled) Color(0xFF4CAF50) else Color(0xFF3A3A3A),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AddCustomAppDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, packageName: String, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(COLOR_PRESETS[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E30),
        title = {
            Text(
                text = "Add Custom App",
                color = Color(0xFFE8E8F0)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFE8E8F0),
                        unfocusedTextColor = Color(0xFFE8E8F0),
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color(0xFF3A3A58),
                        focusedLabelColor = Color(0xFF4CAF50),
                        unfocusedLabelColor = Color(0xFF9898A8),
                        cursorColor = Color(0xFF4CAF50)
                    )
                )

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name") },
                    placeholder = { Text("e.g. com.example.app", color = Color(0xFF6C6C7C)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFE8E8F0),
                        unfocusedTextColor = Color(0xFFE8E8F0),
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color(0xFF3A3A58),
                        focusedLabelColor = Color(0xFF4CAF50),
                        unfocusedLabelColor = Color(0xFF9898A8),
                        cursorColor = Color(0xFF4CAF50)
                    )
                )

                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF9898A8)
                )

                // Color presets row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    COLOR_PRESETS.take(5).forEach { colorHex ->
                        val color = Color(colorHex.toInt())
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) Modifier.shadow(4.dp, CircleShape)
                                    else Modifier
                                )
                                .background(color)
                                .then(
                                    if (isSelected) Modifier.padding(2.dp).clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.3f))
                                    else Modifier
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    COLOR_PRESETS.drop(5).forEach { colorHex ->
                        val color = Color(colorHex.toInt())
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) Modifier.shadow(4.dp, CircleShape)
                                    else Modifier
                                )
                                .background(color)
                                .then(
                                    if (isSelected) Modifier.padding(2.dp).clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.3f))
                                    else Modifier
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), packageName.trim(), selectedColor) },
                enabled = name.isNotBlank() && packageName.isNotBlank()
            ) {
                Text("Save", color = if (name.isNotBlank() && packageName.isNotBlank()) Color(0xFF4CAF50) else Color(0xFF6C6C7C))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF9898A8))
            }
        }
    )
}
