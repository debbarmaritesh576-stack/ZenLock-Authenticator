package com.aegis.pdf.features.scanner.crop

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged
import android.graphics.Bitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerspectiveCropScreen(
    bitmap: Bitmap,
    initialBounds: com.aegis.pdf.features.scanner.model.DocumentBounds,
    onCropComplete: (cropped: Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    var topLeft by remember { mutableStateOf(Offset(initialBounds.topLeft.first, initialBounds.topLeft.second)) }
    var topRight by remember { mutableStateOf(Offset(initialBounds.topRight.first, initialBounds.topRight.second)) }
    var bottomRight by remember { mutableStateOf(Offset(initialBounds.bottomRight.first, initialBounds.bottomRight.second)) }
    var bottomLeft by remember { mutableStateOf(Offset(initialBounds.bottomLeft.first, initialBounds.bottomLeft.second)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adjust Corners") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            // Image with crop overlay
            CropOverlay(
                bitmap = bitmap,
                topLeft = topLeft,
                topRight = topRight,
                bottomRight = bottomRight,
                bottomLeft = bottomLeft,
                onCornerDrag = { corner, newPosition ->
                    when (corner) {
                        Corner.TOP_LEFT -> topLeft = newPosition
                        Corner.TOP_RIGHT -> topRight = newPosition
                        Corner.BOTTOM_RIGHT -> bottomRight = newPosition
                        Corner.BOTTOM_LEFT -> bottomLeft = newPosition
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Bottom buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, "Cancel", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val croppedBitmap = perspectiveCrop(
                            bitmap,
                            topLeft, topRight, bottomRight, bottomLeft
                        )
                        onCropComplete(croppedBitmap)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, "Done", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Done")
                }
            }
        }
    }
}

enum class Corner {
    TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
}

@Composable
fun CropOverlay(
    bitmap: Bitmap,
    topLeft: Offset,
    topRight: Offset,
    bottomRight: Offset,
    bottomLeft: Offset,
    onCornerDrag: (corner: Corner, newPosition: Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var containerSize by remember { mutableStateOf<androidx.compose.ui.unit.IntSize?>(null) }

    Box(modifier = modifier
        .onSizeChanged { containerSize = it }
        .background(Color.Black)
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
            }
        }) {

        AndroidImage(bitmap = bitmap, modifier = Modifier.fillMaxSize())

        Canvas(modifier = Modifier.fillMaxSize()) {
            val paint = androidx.compose.ui.graphics.Paint().apply {
                style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                strokeWidth = 3f
                color = Color.Green
            }

            drawPath(
                androidx.compose.ui.graphics.Path().apply {
                    moveTo(topLeft.x, topLeft.y)
                    lineTo(topRight.x, topRight.y)
                    lineTo(bottomRight.x, bottomRight.y)
                    lineTo(bottomLeft.x, bottomLeft.y)
                    close()
                },
                paint
            )
        }

        // Corner handles
        CornerHandle(
            position = topLeft,
            corner = Corner.TOP_LEFT,
            onDrag = { newPos -> onCornerDrag(Corner.TOP_LEFT, newPos) }
        )
        CornerHandle(
            position = topRight,
            corner = Corner.TOP_RIGHT,
            onDrag = { newPos -> onCornerDrag(Corner.TOP_RIGHT, newPos) }
        )
        CornerHandle(
            position = bottomRight,
            corner = Corner.BOTTOM_RIGHT,
            onDrag = { newPos -> onCornerDrag(Corner.BOTTOM_RIGHT, newPos) }
        )
        CornerHandle(
            position = bottomLeft,
            corner = Corner.BOTTOM_LEFT,
            onDrag = { newPos -> onCornerDrag(Corner.BOTTOM_LEFT, newPos) }
        )
    }
}

@Composable
fun CornerHandle(
    position: Offset,
    corner: Corner,
    onDrag: (Offset) -> Unit,
    size: Float = 40f
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .offset(position.x.dp - (size / 2).dp, position.y.dp - (size / 2).dp)
            .background(Color.Green, shape = androidx.compose.foundation.shape.CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(Offset(position.x + dragAmount.x, position.y + dragAmount.y))
                }
            }
    )
}

@Composable
fun AndroidImage(bitmap: Bitmap, modifier: Modifier = Modifier) {
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

@Composable
fun Canvas(
    modifier: Modifier = Modifier,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}

private fun perspectiveCrop(
    bitmap: Bitmap,
    topLeft: Offset,
    topRight: Offset,
    bottomRight: Offset,
    bottomLeft: Offset
): Bitmap {
    val srcPoints = floatArrayOf(
        topLeft.x, topLeft.y,
        topRight.x, topRight.y,
        bottomRight.x, bottomRight.y,
        bottomLeft.x, bottomLeft.y
    )

    val dstPoints = floatArrayOf(
        0f, 0f,
        bitmap.width.toFloat(), 0f,
        bitmap.width.toFloat(), bitmap.height.toFloat(),
        0f, bitmap.height.toFloat()
    )

    val perspectiveMatrix = getPerspectiveTransform(srcPoints, dstPoints)
    val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

    val srcPixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(srcPixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    val dstPixels = IntArray(bitmap.width * bitmap.height)

    for (y in 0 until bitmap.height) {
        for (x in 0 until bitmap.width) {
            val srcX = ((x * perspectiveMatrix[0] + y * perspectiveMatrix[1] + perspectiveMatrix[2]) /
                       (x * perspectiveMatrix[6] + y * perspectiveMatrix[7] + perspectiveMatrix[8])).toInt()
                .coerceIn(0, bitmap.width - 1)
            val srcY = ((x * perspectiveMatrix[3] + y * perspectiveMatrix[4] + perspectiveMatrix[5]) /
                       (x * perspectiveMatrix[6] + y * perspectiveMatrix[7] + perspectiveMatrix[8])).toInt()
                .coerceIn(0, bitmap.height - 1)

            dstPixels[y * bitmap.width + x] = srcPixels[srcY * bitmap.width + srcX]
        }
    }

    result.setPixels(dstPixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    return result
}

private fun getPerspectiveTransform(src: FloatArray, dst: FloatArray): FloatArray {
    val matrix = FloatArray(9)
    org.opencv.core.Mat.getPerspectiveTransform(
        org.opencv.core.MatOfPoint2f(
            org.opencv.core.Point(src[0].toDouble(), src[1].toDouble()),
            org.opencv.core.Point(src[2].toDouble(), src[3].toDouble()),
            org.opencv.core.Point(src[4].toDouble(), src[5].toDouble()),
            org.opencv.core.Point(src[6].toDouble(), src[7].toDouble())
        ),
        org.opencv.core.MatOfPoint2f(
            org.opencv.core.Point(dst[0].toDouble(), dst[1].toDouble()),
            org.opencv.core.Point(dst[2].toDouble(), dst[3].toDouble()),
            org.opencv.core.Point(dst[4].toDouble(), dst[5].toDouble()),
            org.opencv.core.Point(dst[6].toDouble(), dst[7].toDouble())
        )
    ).get(0, 0, matrix)
    return matrix
}