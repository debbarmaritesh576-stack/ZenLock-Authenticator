package com.aegis.pdf.feature.converter  
  
import androidx.lifecycle.ViewModel  
import androidx.lifecycle.viewModelScope  
import com.aegis.pdf.core.pdf.AdvancedConverterEngine  
import dagger.hilt.android.lifecycle.HiltViewModel  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import kotlinx.coroutines.launch  
import java.io.File  
import javax.inject.Inject  
  
@HiltViewModel  
class AdvancedConversionViewModel @Inject constructor(  
    private val converterEngine: AdvancedConverterEngine  
) : ViewModel() {  
  
    private val _conversionState = MutableStateFlow<ConversionUiState>(ConversionUiState.Idle)  
    val conversionState = _conversionState.asStateFlow()  
  
    fun transformUrl(url: String, outputDir: File, outputName: String) {  
        _conversionState.value = ConversionUiState.Processing("Fetching web matrix strings...")  
        val targetFile = File(outputDir, "$outputName.pdf")  
          
        viewModelScope.launch {  
            converterEngine.convertUrlToPdf(url, targetFile) { success ->  
                _conversionState.value = if (success) ConversionUiState.Success(targetFile)   
                else ConversionUiState.Error("Web rendering timeout crash")  
            }  
        }  
    }  
  
    fun extractTextFromPdf(sourceFile: File, outputDir: File) {  
        _conversionState.value = ConversionUiState.Processing("Extracting lexical tokens...")  
        val targetFile = File(outputDir, "${sourceFile.nameWithoutExtension}_Extracted.txt")  
  
        viewModelScope.launch {  
            converterEngine.convertPdfToText(sourceFile, targetFile)  
                .onSuccess { _conversionState.value = ConversionUiState.Success(it) }  
                .onFailure { _conversionState.value = ConversionUiState.Error(it.localizedMessage ?: "OCR Stream Fail") }  
        }  
    }  
}  
  
sealed interface ConversionUiState {  
    object Idle : ConversionUiState  
    data class Processing(val message: String) : ConversionUiState  
    data class Success(val file: File) : ConversionUiState  
    data class Error(val reason: String) : ConversionUiState  
}