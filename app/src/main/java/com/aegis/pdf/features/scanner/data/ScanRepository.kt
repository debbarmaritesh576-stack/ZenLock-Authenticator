package com.aegis.pdf.features.scanner.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.aegis.pdf.features.scanner.model.ScannerState
import com.aegis.pdf.features.scanner.model.ScanSettings
import com.aegis.pdf.features.scanner.data.ScanRepository

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state: StateFlow<ScannerState> = _state

    private val TAG = "ScannerViewModel"

    fun onFrameCapture(bitmap: Bitmap, settings: ScanSettings) {
        viewModelScope.launch {
            try {
                val (frame, bounds) = scanRepository.processFrame(bitmap, settings)
                
                _state.value = _state.value.copy(
                    currentFrame = frame,
                    documentBounds = bounds,
                    docQuality = bounds?.confidence ?: 0f
                )
                
                Log.d(TAG, "Frame processed: quality=${bounds?.confidence}")
            } catch (e: Exception) {
                Log.e(TAG, "Frame capture failed", e)
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun captureDocument(bitmap: Bitmap, settings: ScanSettings) {
        viewModelScope.launch {
            val bounds = _state.value.documentBounds
            if (bounds == null) {
                _state.value = _state.value.copy(error = "No document detected")
                return@launch
            }

            _state.value = _state.value.copy(isProcessing = true)

            try {
                val scannedUri = scanRepository.enhanceAndSave(bitmap, bounds, settings)
                
                _state.value = _state.value.copy(
                    isProcessing = false,
                    capturedFrames = _state.value.capturedFrames + bitmap
                )
                
                Log.d(TAG, "Document captured: $scannedUri")
            } catch (e: Exception) {
                Log.e(TAG, "Document capture failed", e)
                _state.value = _state.value.copy(
                    isProcessing = false,
                    error = e.message
                )
            }
        }
    }

    fun createPdfFromCaptures() {
        viewModelScope.launch {
            if (_state.value.capturedFrames.isEmpty()) {
                _state.value = _state.value.copy(error = "No scans to create PDF")
                return@launch
            }

            _state.value = _state.value.copy(isProcessing = true)

            try {
                val pdfUri = scanRepository.createPdfFromScans(
                    _state.value.capturedFrames.mapIndexed { idx, _ ->
                        Uri.fromFile(java.io.File(android.os.Environment.getExternalStorageDirectory(), "scan_$idx.jpg"))
                    }
                )

                _state.value = _state.value.copy(
                    isProcessing = false
                )
                
                Log.d(TAG, "PDF created: $pdfUri")
            } catch (e: Exception) {
                Log.e(TAG, "PDF creation failed", e)
                _state.value = _state.value.copy(
                    isProcessing = false,
                    error = e.message
                )
            }
        }
    }

    fun toggleFlash() {
        _state.value = _state.value.copy(flashEnabled = !_state.value.flashEnabled)
    }

    fun toggleAutoFocus() {
        _state.value = _state.value.copy(autoFocus = !_state.value.autoFocus)
    }

    fun setBrightness(brightness: Float) {
        _state.value = _state.value.copy(brightness = brightness)
    }

    fun setContrast(contrast: Float) {
        _state.value = _state.value.copy(contrast = contrast)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearCaptures() {
        viewModelScope.launch {
            scanRepository.clearScans()
            _state.value = _state.value.copy(capturedFrames = emptyList())
        }
    }
}