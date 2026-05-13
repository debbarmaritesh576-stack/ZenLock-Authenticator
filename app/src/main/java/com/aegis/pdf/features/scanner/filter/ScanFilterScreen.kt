package com.aegis.pdf.features.scanner.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.aegis.pdf.features.scanner.processor.DocumentProcessor
import com.aegis.pdf.features.scanner.model.ColorMode
import android.graphics.Bitmap
import kotlinx.coroutines.launch

enum class FilterType {
    ORIGINAL, MAGIC_COLOR, GRAYSCALE, BLACK_WHITE, AUTO_ENHANCE, SHARPEN, CONTRAST, BRIGHTNESS
}

data class FilterPreview(
    val type: FilterType,
    val label: String,
    val icon: androidx.compose.material.icons.materialIcon,
    val bitmap: Bitmap? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanFilterScreen(
    originalBitmap: Bitmap,
    scanIndex: Int,
    onFilterApply: (Bitmap) -> Unit,
    onCancel: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
    documentProcessor: DocumentProcessor = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var selectedFilter by remember { mutableStateOf(FilterType.ORIGINAL) }
    var previewBitmap by remember { mutableStateOf(originalBitmap) }
    var isProcessing by remember { mutableStateOf(false) }
    var brightness by remember { mutableStateOf(1f) }
    var contrast by remember { mutableStateOf(1f) }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply Filters") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    AndroidImageView(bitmap = previewBitmap!!, modifier = Modifier.fillMaxSize())
                } else {
                    CircularProgressIndicator(color = Color.White)
                }

                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(
                    listOf(
                        FilterPreview(FilterType.ORIGINAL, "Original", Icons.Default.PhotoCamera),
                        FilterPreview(FilterType.MAGIC_COLOR, "Magic Color", Icons.Default.Palette),
                        FilterPreview(FilterType.GRAYSCALE, "Grayscale", Icons.Default.Tonality),
                        FilterPreview(FilterType.BLACK_WHITE, "B&W", Icons.Default.ContrastSharp),
                        FilterPreview(FilterType.AUTO_ENHANCE, "Auto", Icons.Default.AutoAwesome),
                        FilterPreview(FilterType.SHARPEN, "Sharp", Icons.Default.Opacity),
                        FilterPreview(FilterType.CONTRAST, "Contrast", Icons.Default.BrightnessHigh),
                        FilterPreview(FilterType.BRIGHTNESS, "Bright", Icons.Default.WbSunny)
                    )
                ) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter.type,
                        onClick = {
                            selectedFilter = filter.type
                            isProcessing = true

                            scope.launch {
                                try {
                                    val filtered = when (filter.type) {
                                        FilterType.ORIGINAL -> originalBitmap
                                        FilterType.MAGIC_COLOR -> applyMagicColor(originalBitmap)
                                        FilterType.GRAYSCALE -> applyGrayscale(originalBitmap)
                                        FilterType.BLACK_WHITE -> applyBlackWhite(originalBitmap)
                                        FilterType.AUTO_ENHANCE -> applyAutoEnhance(originalBitmap)
                                        FilterType.SHARPEN -> applySharpen(originalBitmap)
                                        FilterType.CONTRAST -> applyContrast(originalBitmap, 1.3f)
                                        FilterType.BRIGHTNESS -> applyBrightness(originalBitmap, 1.1f)
                                    }
                                    previewBitmap = filtered
                                    isProcessing = false
                                } catch (e: Exception) {
                                    isProcessing = false
                                }
                            }
                        },
                        label = { Text(filter.label) },
                        leadingIcon = {
                            Icon(filter.icon, null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }

            if (showAdvancedSettings) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Advanced Settings", style = MaterialTheme.typography.titleSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Brightness", modifier = Modifier.weight(0.3f))
                        Slider(
                            value = brightness,
                            onValueChange = { brightness = it },
                            valueRange = 0.5f..2f,
                            modifier = Modifier.weight(0.7f)
                        )
                        Text("${String.format("%.1f", brightness)}x", modifier = Modifier.width(40.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Contrast", modifier = Modifier.weight(0.3f))
                        Slider(
                            value = contrast,
                            onValueChange = { contrast = it },
                            valueRange = 0.5f..2f,
                            modifier = Modifier.weight(0.7f)
                        )
                        Text("${String.format("%.1f", contrast)}x", modifier = Modifier.width(40.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    previewBitmap = applyBrightness(
                                        applyContrast(originalBitmap, contrast),
                                        brightness
                                    )
                                    isProcessing = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Preview")
                        }

                        Button(
                            onClick = {
                                brightness = 1f
                                contrast = 1f
                                previewBitmap = originalBitmap
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showAdvancedSettings = !showAdvancedSettings },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Advanced")
                }

                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cancel")
                }

                Button(
                    onClick = { onFilterApply(previewBitmap) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
fun AndroidImageView(bitmap: Bitmap, modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            android.widget.ImageView(context).apply {
                setImageBitmap(bitmap)
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            }
        },
        modifier = modifier
    )
}

private fun applyMagicColor(bitmap: Bitmap): Bitmap {
    val colorMatrix = android.graphics.ColorMatrix().apply {
        set(floatArrayOf(
            1.1f, 0f, 0f, 0f, 0f,
            0f, 1.05f, 0f, 0f, 0f,
            0f, 0f, 0.95f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
    }

    val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    val canvas = android.graphics.Canvas(result)
    val paint = android.graphics.Paint().apply {
        colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
    }
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    return result
}

private fun applyGrayscale(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in pixels.indices) {
        val r = (pixels[i] shr 16) and 0xFF
        val g = (pixels[i] shr 8) and 0xFF
        val b = pixels[i] and 0xFF

        val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        pixels[i] = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
    }

    val result = Bitmap.createBitmap(width, height, bitmap.config)
    result.setPixels(pixels, 0, width, 0, 0, width, height)
    return result
}

private fun applyBlackWhite(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val gray = applyGrayscale(bitmap)
    val pixels = IntArray(width * height)
    gray.getPixels(pixels, 0, width, 0, 0, width, height)

    var totalBrightness = 0L
    for (pixel in pixels) {
        totalBrightness += (pixel shr 16) and 0xFF
    }
    val threshold = (totalBrightness / pixels.size).toInt()

    for (i in pixels.indices) {
        val brightness = (pixels[i] shr 16) and 0xFF
        val bw = if (brightness > threshold) 255 else 0
        pixels[i] = (0xFF shl 24) or (bw shl 16) or (bw shl 8) or bw
    }

    val result = Bitmap.createBitmap(width, height, bitmap.config)
    result.setPixels(pixels, 0, width, 0, 0, width, height)
    return result
}

private fun applyAutoEnhance(bitmap: Bitmap): Bitmap {
    var enhanced = applyContrast(bitmap, 1.2f)
    enhanced = applyBrightness(enhanced, 1.1f)
    return enhanced
}

private fun applySharpen(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val srcPixels = IntArray(width * height)
    bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)

    val dstPixels = IntArray(width * height)

    val kernel = floatArrayOf(
        -1f, -1f, -1f,
        -1f, 9f, -1f,
        -1f, -1f, -1f
    )

    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            var r = 0f
            var g = 0f
            var b = 0f

            for (ky in -1..1) {
                for (kx in -1..1) {
                    val pixel = srcPixels[(y + ky) * width + (x + kx)]
                    val kr = (pixel shr 16) and 0xFF
                    val kg = (pixel shr 8) and 0xFF
                    val kb = pixel and 0xFF

                    val weight = kernel[(ky + 1) * 3 + (kx + 1)]
                    r += kr * weight
                    g += kg * weight
                    b += kb * weight
                }
            }

            val finalR = (r / 9).toInt().coerceIn(0, 255)
            val finalG = (g / 9).toInt().coerceIn(0, 255)
            val finalB = (b / 9).toInt().coerceIn(0, 255)

            dstPixels[y * width + x] = (0xFF shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
        }
    }

    val result = Bitmap.createBitmap(width, height, bitmap.config)
    result.setPixels(dstPixels, 0, width, 0, 0, width, height)
    return result
}

private fun applyContrast(bitmap: Bitmap, contrast: Float): Bitmap {
    val colorMatrix = android.graphics.ColorMatrix().apply {
        set(floatArrayOf(
            contrast, 0f, 0f, 0f, (1 - contrast) * 128,
            0f, contrast, 0f, 0f, (1 - contrast) * 128,
            0f, 0f, contrast, 0f, (1 - contrast) * 128,
            0f, 0f, 0f, 1f, 0f
        ))
    }

    val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    val canvas = android.graphics.Canvas(result)
    val paint = android.graphics.Paint().apply {
        colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
    }
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    return result
}

private fun applyBrightness(bitmap: Bitmap, brightness: Float): Bitmap {
    val colorMatrix = android.graphics.ColorMatrix().apply {
        set(floatArrayOf(
            1f, 0f, 0f, 0f, (brightness - 1) * 255,
            0f, 1f, 0f, 0f, (brightness - 1) * 255,
            0f, 0f, 1f, 0f, (brightness - 1) * 255,
            0f, 0f, 0f, 1f, 0f
        ))
    }

    val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    val canvas = android.graphics.Canvas(result)
    val paint = android.graphics.Paint().apply {
        colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
    }
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    return result
}