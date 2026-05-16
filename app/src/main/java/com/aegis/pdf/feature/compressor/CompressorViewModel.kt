package com.aegis.pdf.feature.compressor  
  
import androidx.lifecycle.ViewModel  
import androidx.lifecycle.viewModelScope  
import com.aegis.pdf.core.pdf.PdfCompressorEngine  
import dagger.hilt.android.lifecycle.HiltViewModel  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import kotlinx.coroutines.launch  
import java.io.File  
import javax.inject.Inject  
  
@HiltViewModel  
class CompressorViewModel @Inject constructor(  
    private val compressorEngine: PdfCompressorEngine  
) : ViewModel() {  
  
    private val _compressState = MutableStateFlow<CompressUiState>(CompressUiState.Idle)  
    val compressState = _compressState.asStateFlow()  
  
    fun startCompression(sourceFile: File, outputDir: File, level: CompressionLevel) {  
        viewModelScope.launch {  
            _compressState.value = CompressUiState.Loading  
            val targetFile = File(outputDir, "Aegis_Compressed_${sourceFile.name}")  
              
            // Quality levels map: HIGH_COMPRESSION = lower quality float, etc.  
            val qualityFactor = when (level) {  
                CompressionLevel.HIGH -> 0.3f  // Squeezes down to ~70% less size  
                CompressionLevel.MEDIUM -> 0.6f // Standard web optimized balance  
                CompressionLevel.LOW -> 0.85f  // Text crisp preservation mode  
            }  
  
            compressorEngine.compressPdf(sourceFile, targetFile, qualityFactor)  
                .onSuccess { _compressState.value = CompressUiState.Success(it) }  
                .onFailure { _compressState.value = CompressUiState.Error(it.localizedMessage ?: "Compression crash") }  
        }  
    }  
}  
  
enum class CompressionLevel { LOW, MEDIUM, HIGH }  
  
sealed interface CompressUiState {  
    object Idle : CompressUiState  
    object Loading : CompressUiState  
    data class Success(val file: File) : CompressUiState  
    data class Error(val message: String) : CompressUiState  
}