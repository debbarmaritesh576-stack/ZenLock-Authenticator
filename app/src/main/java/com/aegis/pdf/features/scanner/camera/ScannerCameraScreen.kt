package com.aegis.pdf.features.scanner.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aegis.pdf.features.scanner.viewmodel.ScannerViewModel
import androidx.camera.view.PreviewView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerCameraScreen(
    onBack: () -> Unit,
    onCapture: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
    cameraController: com.aegis.pdf.features.scanner.camera.CameraController
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var showPermissionRequest by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(cameraController.hasPermissions()) }

    if (!hasCameraPermission && !showPermissionRequest) {
        showPermissionRequest = true
    }

    LaunchedEffect(Unit) {
        if (hasCameraPermission) {
            val imageAnalyzer = FrameAnalyzer { imageProxy ->
                // Handle frame
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Document") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFlash() }) {
                        Icon(
                            if (state.flashEnabled) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            "Flash"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onSurfaceProviderReady = { surfaceProvider ->
                        try {
                            cameraController.bindCamera(
                                lifecycleOwner,
                                surfaceProvider,
                                FrameAnalyzer { imageProxy ->
                                    // Process frame
                                }
                            )
                        } catch (e: Exception) {
                            viewModel.clearError()
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            "Camera",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Camera permission required", color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { hasCameraPermission = true }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Document bounds overlay
            if (state.documentBounds != null) {
                DocumentBoundsOverlay(state.documentBounds!!)
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quality indicator
                LinearProgressIndicator(
                    progress = { state.docQuality },
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        state.docQuality > 0.8f -> Color.Green
                        state.docQuality > 0.6f -> Color.Yellow
                        else -> Color.Red
                    }
                )

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleAutoFocus() },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (state.autoFocus) Color.Blue else Color.Gray,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.AutoFix,
                            "AutoFocus",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Button(
                        onClick = {
                            onCapture()
                            viewModel.captureDocument(state.currentFrame!!, com.aegis.pdf.features.scanner.model.ScanSettings())
                        },
                        modifier = Modifier
                            .size(80.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        enabled = state.currentFrame != null && !state.isProcessing
                    ) {
                        Icon(
                            Icons.Default.RadioButtonChecked,
                            "Capture",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = { cameraController.unbindCamera() },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.Red, shape = androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Cancel",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Capture count
                Text(
                    "Scans: ${state.capturedFrames.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
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
fun DocumentBoundsOverlay(
    bounds: com.aegis.pdf.features.scanner.model.DocumentBounds
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val paint = androidx.compose.ui.graphics.Paint().apply {
            style = androidx.compose.ui.graphics.PaintingStyle.Stroke
            strokeWidth = 4f
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

@Composable
fun Canvas(
    modifier: Modifier = Modifier,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}