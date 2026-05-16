package com.aegis.pdf.feature.splitter  
  
import androidx.lifecycle.ViewModel  
import androidx.lifecycle.viewModelScope  
import com.aegis.pdf.core.pdf.PdfSplitter  
import dagger.hilt.android.lifecycle.HiltViewModel  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import kotlinx.coroutines.launch  
import java.io.File  
import javax.inject.Inject  
  
@HiltViewModel  
class SplitViewModel @Inject constructor(  
    private val splitter: PdfSplitter  
) : ViewModel() {  
  
    private val _selectedPages = MutableStateFlow<Set<Int>>(emptySet())  
    val selectedPages = _selectedPages.asStateFlow()  
  
    private val _splitState = MutableStateFlow<SplitUiState>(SplitUiState.Idle)  
    val splitState = _splitState.asStateFlow()  
  
    fun togglePage(index: Int) {  
        val current = _selectedPages.value.toMutableSet()  
        if (current.contains(index)) current.remove(index) else current.add(index)  
        _selectedPages.value = current  
    }  
  
    fun executeSplit(sourceFile: File, outputDir: File, outputName: String) {  
        if (_selectedPages.value.isEmpty()) return  
  
        viewModelScope.launch {  
            _splitState.value = SplitUiState.Loading  
            val finalFile = File(outputDir, if (outputName.endsWith(".pdf")) outputName else "$outputName.pdf")  
              
            splitter.splitSpecificPages(sourceFile, _selectedPages.value.toList().sorted(), finalFile)  
                .onSuccess { _splitState.value = SplitUiState.Success(it) }  
                .onFailure { _splitState.value = SplitUiState.Error(it.localizedMessage ?: "Slicing failed") }  
        }  
    }  
}  
  
sealed interface SplitUiState {  
    object Idle : SplitUiState  
    object Loading : SplitUiState  
    data class Success(val file: File) : SplitUiState  
    data class Error(val message: String) : SplitUiState  
}