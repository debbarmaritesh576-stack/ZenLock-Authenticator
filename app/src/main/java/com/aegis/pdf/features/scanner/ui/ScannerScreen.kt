package com.aegis.pdf.features.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.features.scanner.viewmodel.ScannerViewModel
import com.aegis.pdf.features.scanner.model.ScanResolution
import com.aegis.pdf.features.scanner.model.ColorMode
import com.aegis.pdf.features.scanner.model.ScanSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onScanComplete: (String) -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(ScanSettings()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color.Black)) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.currentFrame != null) {
                        AndroidView(
                            factory = { context ->
                                android.widget.ImageView(context).apply {
                                    setImageBitmap(state.currentFrame)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Waiting for camera...", color = Color.White)
                    }

                    if (state.documentBounds != null) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val bounds = state.documentBounds!!
                            val paint = androidx.compose.ui.graphics.Paint().apply {
                                style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                strokeWidth = 3f
                                color = Color.Green
                            }

                            drawPath(
                                androidx.compose.ui.graphics.Path().apply {
                                    moveTo(bounds.topLeft.first, bounds.topLeft.second)
                                    lineTo(bounds.topRight.first, bounds.topRight.second)
                                    lineTo(bounds.bottomRight.first, bounds.bottomRight.second)
                                    lineTo(bounds.bottomLeft.first, bounds.bottomLeft.second)
                                    close()
                                },
                                paint
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleFlash() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (state.flashEnabled) Color.Yellow else Color.Gray,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.FlashlightOn,
                            "Flash",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.captureDocument(state.currentFrame!!, settings) },
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.CenterVertically),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        enabled = state.currentFrame != null && !state.isProcessing
                    ) {
                        Icon(Icons.Default.RadioButtonChecked, "Capture", tint = Color.White)
                    }

                    IconButton(
                        onClick = { viewModel.toggleAutoFocus() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (state.autoFocus) Color.Blue else Color.Gray,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.AutoFix,
                            "AutoFocus",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Quality: ${(state.docQuality * 100).toInt()}%",
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    )
                    Button(
                        onClick = { viewModel.createPdfFromCaptures() },
                        enabled = state.capturedFrames.isNotEmpty() && !state.isProcessing
                    ) {
                        Icon(Icons.Default.FileCopy, "Create PDF", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Create PDF (${state.capturedFrames.size})")
                    }
                }
            }

            if (state.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            if (showSettings) {
                ScannerSettingsDialog(
                    settings = settings,
                    onSettingsChange = { settings = it },
                    onDismiss = { showSettings = false }
                )
            }

            if (state.error != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Error") },
                    text = { Text(state.error!!) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ScannerSettingsDialog(
    settings: ScanSettings,
    onSettingsChange: (ScanSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan Settings") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Resolution")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ScanResolution.values().forEach { res ->
                            FilterChip(
                                selected = settings.resolution == res,
                                onClick = { onSettingsChange(settings.copy(resolution = res)) },
                                label = { Text(res.label) }
                            )
                        }
                    }
                }

                item {
                    Text("Color Mode")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ColorMode.values().forEach { mode ->
                            FilterChip(
                                selected = settings.colorMode == mode,
                                onClick = { onSettingsChange(settings.copy(colorMode = mode)) },
                                label = { Text(mode.label) }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto Enhance")
                        Switch(
                            checked = settings.autoEnhance,
                            onCheckedChange = { onSettingsChange(settings.copy(autoEnhance = it)) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Detect Edges")
                        Switch(
                            checked = settings.detectEdges,
                            onCheckedChange = { onSettingsChange(settings.copy(detectEdges = it)) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Perspective Correction")
                        Switch(
                            checked = settings.perspectiveCorrection,
                            onCheckedChange = { onSettingsChange(settings.copy(perspectiveCorrection = it)) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Magic Color")
                        Switch(
                            checked = settings.enableMagicColor,
                            onCheckedChange = { onSettingsChange(settings.copy(enableMagicColor = it)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun AndroidView(factory: (android.content.Context) -> android.view.View, modifier: Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = factory,
        modifier = modifier
    )
}

@Composable
fun Canvas(modifier: Modifier, onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}