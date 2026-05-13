package com.aegis.pdf.features.annotation.viewmodel

import android.graphics.PointF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.aegis.pdf.features.annotation.model.Annotation
import com.aegis.pdf.features.annotation.model.AnnotationPoint
import com.aegis.pdf.features.annotation.model.AnnotationState
import com.aegis.pdf.features.annotation.model.AnnotationStyle
import com.aegis.pdf.features.annotation.model.AnnotationType
import com.aegis.pdf.features.annotation.engine.AnnotationEngine
import androidx.compose.ui.graphics.Color

@HiltViewModel
class AnnotationViewModel @Inject constructor(
    private val annotationEngine: AnnotationEngine
) : ViewModel() {

    private val _state = MutableStateFlow(AnnotationState())
    val state: StateFlow<AnnotationState> = _state

    private val TAG = "AnnotationViewModel"

    fun setCurrentAnnotationType(type: AnnotationType) {
        _state.value = _state.value.copy(currentType = type)
        Log.d(TAG, "Annotation type changed: $type")
    }

    fun setCurrentStyle(style: AnnotationStyle) {
        _state.value = _state.value.copy(currentStyle = style)
        Log.d(TAG, "Annotation style changed")
    }

    fun startDrawing() {
        _state.value = _state.value.copy(
            isDrawing = true,
            currentPoints = emptyList()
        )
        Log.d(TAG, "Drawing started")
    }

    fun addPoint(point: PointF, pressure: Float = 1f) {
        if (!_state.value.isDrawing) return

        val annotationPoint = AnnotationPoint(
            x = point.x,
            y = point.y,
            pressure = pressure
        )

        val currentPoints = _state.value.currentPoints.toMutableList()
        currentPoints.add(annotationPoint)
        _state.value = _state.value.copy(currentPoints = currentPoints)
    }

    fun finishDrawing() {
        viewModelScope.launch {
            val currentPoints = _state.value.currentPoints
            if (currentPoints.isEmpty()) {
                _state.value = _state.value.copy(isDrawing = false)
                return@launch
            }

            try {
                val simplifiedPoints = annotationEngine.simplifyPath(currentPoints)
                val smoothedPoints = annotationEngine.smoothPath(simplifiedPoints)

                val detectedType = annotationEngine.detectDrawingGesture(smoothedPoints)
                val finalType = if (_state.value.currentType == AnnotationType.FREEHAND) {
                    detectedType
                } else {
                    _state.value.currentType
                }

                val annotation = annotationEngine.createAnnotation(
                    type = finalType,
                    pageNumber = _state.value.pageNumber,
                    points = smoothedPoints,
                    color = _state.value.currentStyle.color,
                    strokeWidth = _state.value.currentStyle.strokeWidth
                )

                if (annotation != null) {
                    val annotations = _state.value.annotations.toMutableList()
                    annotations.add(annotation)

                    _state.value = _state.value.copy(
                        isDrawing = false,
                        currentPoints = emptyList(),
                        annotations = annotations,
                        undoStack = _state.value.undoStack + annotation
                    )

                    Log.d(TAG, "Drawing finished: ${annotation.type}, points=${smoothedPoints.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to finish drawing", e)
                _state.value = _state.value.copy(isDrawing = false)
            }
        }
    }

    fun selectAnnotation(annotationId: String) {
        val annotation = _state.value.annotations.find { it.id == annotationId }
        if (annotation != null) {
            val updatedAnnotations = _state.value.annotations.map {
                it.copy(isSelected = it.id == annotationId)
            }
            _state.value = _state.value.copy(
                annotations = updatedAnnotations,
                selectedAnnotationId = annotationId
            )
            Log.d(TAG, "Annotation selected: $annotationId")
        }
    }

    fun deleteAnnotation(annotationId: String) {
        val annotations = _state.value.annotations.filter { it.id != annotationId }
        _state.value = _state.value.copy(
            annotations = annotations,
            selectedAnnotationId = if (_state.value.selectedAnnotationId == annotationId) null else _state.value.selectedAnnotationId
        )
        Log.d(TAG, "Annotation deleted: $annotationId")
    }

    fun modifyAnnotation(annotation: Annotation) {
        val annotations = _state.value.annotations.map {
            if (it.id == annotation.id) annotation else it
        }
        _state.value = _state.value.copy(annotations = annotations)
        Log.d(TAG, "Annotation modified: ${annotation.id}")
    }

    fun changeAnnotationColor(annotationId: String, color: Color) {
        val annotation = _state.value.annotations.find { it.id == annotationId }?.copy(color = color)
        if (annotation != null) {
            modifyAnnotation(annotation)
        }
    }

    fun changeAnnotationOpacity(annotationId: String, opacity: Float) {
        val annotation = _state.value.annotations.find { it.id == annotationId }?.copy(opacity = opacity)
        if (annotation != null) {
            modifyAnnotation(annotation)
        }
    }

    fun undo() {
        if (_state.value.undoStack.isEmpty()) return

        val undoStack = _state.value.undoStack.toMutableList()
        val lastAnnotation = undoStack.removeAt(undoStack.size - 1)

        val annotations = _state.value.annotations.filter { it.id != lastAnnotation.id }
        val redoStack = _state.value.redoStack.toMutableList()
        redoStack.add(lastAnnotation)

        _state.value = _state.value.copy(
            annotations = annotations,
            undoStack = undoStack,
            redoStack = redoStack
        )

        Log.d(TAG, "Undo performed")
    }

    fun redo() {
        if (_state.value.redoStack.isEmpty()) return

        val redoStack = _state.value.redoStack.toMutableList()
        val annotation = redoStack.removeAt(redoStack.size - 1)

        val annotations = _state.value.annotations.toMutableList()
        annotations.add(annotation)

        val undoStack = _state.value.undoStack.toMutableList()
        undoStack.add(annotation)

        _state.value = _state.value.copy(
            annotations = annotations,
            undoStack = undoStack,
            redoStack = redoStack
        )

        Log.d(TAG, "Redo performed")
    }

    fun clearAllAnnotations() {
        _state.value = _state.value.copy(
            annotations = emptyList(),
            undoStack = emptyList(),
            redoStack = emptyList(),
            selectedAnnotationId = null
        )
        Log.d(TAG, "All annotations cleared")
    }

    fun exportAnnotations(format: String = "JSON"): String {
        return annotationEngine.exportAnnotations(_state.value.annotations, format)
    }

    fun importAnnotations(data: String, format: String = "JSON") {
        val imported = annotationEngine.importAnnotations(data, format)
        _state.value = _state.value.copy(annotations = imported)
        Log.d(TAG, "Annotations imported: ${imported.size} items")
    }

    fun setPageNumber(pageNumber: Int) {
        _state.value = _state.value.copy(pageNumber = pageNumber)
    }

    fun hitTest(point: PointF): String? {
        _state.value.annotations.forEach { annotation ->
            if (annotationEngine.hitTest(point, annotation)) {
                return annotation.id
            }
        }
        return null
    }
}