package com.aegis.pdf.features.scanner.camera

import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import android.content.Context

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onSurfaceProviderReady: (PreviewView.SurfaceProvider) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    onSurfaceProviderReady(surfaceProvider)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

class FrameAnalyzer(
    private val onFrameAnalyzed: (androidx.camera.core.ImageProxy) -> Unit
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
        onFrameAnalyzed(imageProxy)
        imageProxy.close()
    }
}