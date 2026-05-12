package com.aegis.pdf.features.scanner.model

import android.graphics.Bitmap

data class ScannerState(
    val isScanning: Boolean = false,
    val currentFrame: Bitmap? = null,
    val capturedFrames: List<Bitmap> = emptyList(),
    val documentBounds: DocumentBounds? = null,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val brightness: Float = 1f,
    val contrast: Float = 1f,
    val autoFocus: Boolean = true,
    val flashEnabled: Boolean = false,
    val resolution: ScanResolution = ScanResolution.HIGH,
    val colorMode: ColorMode = ColorMode.COLOR,
    val docQuality: Float = 0f
)

data class DocumentBounds(
    val topLeft: Pair<Float, Float>,
    val topRight: Pair<Float, Float>,
    val bottomLeft: Pair<Float, Float>,
    val bottomRight: Pair<Float, Float>,
    val confidence: Float
)

enum class ScanResolution(val dpi: Int, val label: String) {
    LOW(150, "150 DPI - Fast"),
    MEDIUM(200, "200 DPI - Balanced"),
    HIGH(300, "300 DPI - High Quality"),
    ULTRA(600, "600 DPI - Ultra")
}

enum class ColorMode(val label: String) {
    COLOR("Color"),
    GRAYSCALE("Grayscale"),
    BLACK_WHITE("B&W"),
    AUTO("Auto")
}

data class ScanSettings(
    val resolution: ScanResolution = ScanResolution.HIGH,
    val colorMode: ColorMode = ColorMode.AUTO,
    val autoEnhance: Boolean = true,
    val detectEdges: Boolean = true,
    val perspectiveCorrection: Boolean = true,
    val removeGlare: Boolean = true,
    val removeNoise: Boolean = true,
    val increaseContrast: Boolean = true,
    val enableOcr: Boolean = false,
    val enableMagicColor: Boolean = false,
    val batchMode: Boolean = false
)

data class ScanResult(
    val originalBitmap: Bitmap,
    val processedBitmap: Bitmap,
    val documentBounds: DocumentBounds,
    val quality: Float,
    val processingTime: Long,
    val extractedText: String? = null
)