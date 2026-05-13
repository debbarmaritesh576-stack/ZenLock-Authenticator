package com.aegis.pdf.features.annotation.ui.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.features.annotation.viewmodel.AnnotationViewModel
import com.aegis.pdf.features.annotation.model.AnnotationType
import android.graphics.PointF

@Composable
fun AnnotationCanvas(
    imageWidth: Float,
    imageHeight: Float,
    viewModel: AnnotationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        viewModel.startDrawing()
                        viewModel.addPoint(PointF(offset.x, offset.y))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        viewModel.addPoint(PointF(change.position.x, change.position.y))
                    },
                    onDragEnd = {
                        viewModel.finishDrawing()
                    }
                )
            }
    ) {
        state.annotations.forEach { annotation ->
            drawAnnotation(annotation)
        }

        if (state.isDrawing && state.currentPoints.isNotEmpty()) {
            drawCurrentAnnotation(
                points = state.currentPoints,
                type = state.currentType,
                color = state.currentStyle.color,
                strokeWidth = state.currentStyle.strokeWidth
            )
        }
    }
}

private fun androidx.compose.foundation.Canvas(
    modifier: Modifier,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier,
        onDraw = onDraw
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAnnotation(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    when (annotation.type) {
        AnnotationType.HIGHLIGHT -> drawHighlight(annotation)
        AnnotationType.UNDERLINE -> drawUnderline(annotation)
        AnnotationType.STRIKEOUT -> drawStrikeout(annotation)
        AnnotationType.FREEHAND -> drawFreehand(annotation)
        AnnotationType.ARROW -> drawArrow(annotation)
        AnnotationType.LINE -> drawLine(annotation)
        AnnotationType.RECTANGLE -> drawRectangle(annotation)
        AnnotationType.CIRCLE -> drawCircleShape(annotation)
        AnnotationType.SQUIGGLY -> drawSquiggly(annotation)
        else -> {}
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHighlight(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    val bounds = annotation.bounds ?: return
    drawRect(
        color = annotation.color,
        topLeft = androidx.compose.ui.geometry.Offset(bounds.left, bounds.top),
        size = androidx.compose.ui.geometry.Size(bounds.width, bounds.height),
        alpha = annotation.opacity
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUnderline(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    val bounds = annotation.bounds ?: return
    drawLine(
        color = annotation.color,
        start = androidx.compose.ui.geometry.Offset(bounds.left, bounds.bottom),
        end = androidx.compose.ui.geometry.Offset(bounds.right, bounds.bottom),
        strokeWidth = annotation.strokeWidth
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStrikeout(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    val bounds = annotation.bounds ?: return
    val midY = (bounds.top + bounds.bottom) / 2
    drawLine(
        color = annotation.color,
        start = androidx.compose.ui.geometry.Offset(bounds.left, midY),
        end = androidx.compose.ui.geometry.Offset(bounds.right, midY),
        strokeWidth = annotation.strokeWidth
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFreehand(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    val points = annotation.points
    for (i in 1 until points.size) {
        drawLine(
            color = annotation.color,
            start = androidx.compose.ui.geometry.Offset(points[i - 1].x, points[i - 1].y),
            end = androidx.compose.ui.geometry.Offset(points[i].x, points[i].y),
            strokeWidth = annotation.strokeWidth
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    val startPoint = annotation.startPoint ?: return
    val endPoint = annotation.endPoint ?: return

    drawLine(
        color = annotation.color,
        start = androidx.compose.ui.geometry.Offset(startPoint.x, startPoint.y),
        end = androidx.compose.ui.geometry.Offset(endPoint.x, endPoint.y),
        strokeWidth = annotation.strokeWidth
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLine(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    val startPoint = annotation.startPoint ?: return
    val endPoint = annotation.endPoint ?: return

    drawLine(
        color = annotation.color,
        start = androidx.compose.ui.geometry.Offset(startPoint.x, startPoint.y),
        end = androidx.compose.ui.geometry.Offset(endPoint.x, endPoint.y),
        strokeWidth = annotation.strokeWidth
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRectangle(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    val bounds = annotation.bounds ?: return
    drawRect(
        color = annotation.color,
        topLeft = androidx.compose.ui.geometry.Offset(bounds.left, bounds.top),
        size = androidx.compose.ui.geometry.Size(bounds.width, bounds.height),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = annotation.strokeWidth)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCircleShape(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    val bounds = annotation.bounds ?: return
    val center = androidx.compose.ui.geometry.Offset(
        bounds.left + bounds.width / 2,
        bounds.top + bounds.height / 2
    )
    val radius = bounds.width / 2

    drawCircle(
        color = annotation.color,
        center = center,
        radius = radius,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = annotation.strokeWidth)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSquiggly(
    annotation: com.aegis.pdf.features.annotation.model.Annotation
) {
    val bounds = annotation.bounds ?: return
    val amplitude = 3f
    val frequency = 0.05f
    val startY = bounds.bottom

    for (x in bounds.left.toInt() until bounds.right.toInt()) {
        val offset = (x - bounds.left) * frequency
        val y = startY + kotlin.math.sin(offset) * amplitude
        val nextX = x + 1
        val nextOffset = (nextX - bounds.left) * frequency
        val nextY = startY + kotlin.math.sin(nextOffset) * amplitude

        drawLine(
            color = annotation.color,
            start = androidx.compose.ui.geometry.Offset(x.toFloat(), y),
            end = androidx.compose.ui.geometry.Offset(nextX.toFloat(), nextY),
            strokeWidth = annotation.strokeWidth
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCurrentAnnotation(
    points: List<com.aegis.pdf.features.annotation.model.AnnotationPoint>,
    type: AnnotationType,
    color: Color,
    strokeWidth: Float
) {
    when (type) {
        AnnotationType.FREEHAND -> {
            for (i in 1 until points.size) {
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(points[i - 1].x, points[i - 1].y),
                    end = androidx.compose.ui.geometry.Offset(points[i].x, points[i].y),
                    strokeWidth = strokeWidth
                )
            }
        }
        else -> {}
    }
}