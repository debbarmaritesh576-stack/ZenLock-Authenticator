package com.aegis.pdf.features.editor.annotation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun AnnotationCanvas(
    toolType: AnnotationToolType,
    annotations: List<Any>,
    onAnnotationAdded: (Any) -> Unit,
    onAnnotationUpdated: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    val highlightTool = remember { HighlightTool() }
    val freehandPen = remember { FreehandPen() }
    val arrowTool = remember { ArrowTool() }
    val eraserTool = remember { EraserTool() }

    Canvas(
        modifier = modifier.pointerInput(toolType) {
            detectTapGestures { offset ->
                when (toolType) {
                    AnnotationToolType.TEXT_COMMENT -> { /* open comment dialog */ }
                    AnnotationToolType.STICKY_NOTE -> { /* open note dialog */ }
                    else -> {}
                }
            }
        }
        .pointerInput(toolType) {
            detectDragGestures(
                onDragStart = { offset ->
                    when (toolType) {
                        AnnotationToolType.HIGHLIGHT -> highlightTool.start(offset)
                        AnnotationToolType.FREEHAND -> freehandPen.start(offset)
                        AnnotationToolType.ARROW -> arrowTool.start(offset)
                        AnnotationToolType.ERASER -> eraserTool.start(offset)
                        else -> {}
                    }
                },
                onDrag = { _, dragAmount ->
                    // handled in onDragStart paths
                },
                onDragEnd = {
                    when (toolType) {
                        AnnotationToolType.HIGHLIGHT -> highlightTool.end()?.let { onAnnotationAdded(it) }
                        AnnotationToolType.FREEHAND -> freehandPen.end()?.let { onAnnotationAdded(it) }
                        AnnotationToolType.ARROW -> arrowTool.end(Offset.Zero)?.let { onAnnotationAdded(it) }
                        AnnotationToolType.ERASER -> eraserTool.end()
                        else -> {}
                    }
                }