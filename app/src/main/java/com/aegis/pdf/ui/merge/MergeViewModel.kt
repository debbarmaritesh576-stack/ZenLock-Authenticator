package com.aegis.pdf.feature.merger  
  
import androidx.lifecycle.ViewModel  
import androidx.lifecycle.viewModelScope  
import com.aegis.pdf.core.pdf.PdfMergerEngine  
import dagger.hilt.android.lifecycle.HiltViewModel  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import kotlinx.coroutines.launch  
import java.io.File  
import javax.inject.Inject  
  
@HiltViewModel  
class MergeViewModel @Inject constructor(  
    private val mergerEngine: PdfMergerEngine  
) : ViewModel() {  
  
    private val _selectedFiles = MutableStateFlow<List<File>>(emptyList())  
    val selectedFiles = _selectedFiles.asStateFlow()  
  
    private val _uiState = MutableStateFlow<MergeUiState>(MergeUiState.Idle)  
    val uiState = _uiState.asStateFlow()  
  
    fun addFiles(files: List<File>) {  
        _selectedFiles.value = _selectedFiles.value + files.filter { it.exists() }  
    }  
  
    fun removeFile(file: File) {  
        _selectedFiles.value = _selectedFiles.value - file  
    }  
  
    // Enterprise Feature: User files ka sequence change kar sake (Drag & Drop)  
    fun reorderFiles(fromIndex: Int, toIndex: Int) {  
        val currentList = _selectedFiles.value.toMutableList()  
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {  
            val movedItem = currentList.removeAt(fromIndex)  
            currentList.add(toIndex, movedItem)  
            _selectedFiles.value = currentList  
        }  
    }  
  
    fun startMerging(outputDir: File, outputName: String) {  
        if (_selectedFiles.value.size < 2) {  
            _uiState.value = MergeUiState.Error("Select at least 2 files to merge.")  
            return  
        }  
  
        viewModelScope.launch {  
            _uiState.value = MergeUiState.Loading  
            val cleanName = if (outputName.endsWith(".pdf", true)) outputName else "$outputName.pdf"  
            val targetFile = File(outputDir, cleanName)  
  
            mergerEngine.mergeDocuments(_selectedFiles.value, targetFile)  
                .onSuccess { mergedFile ->  
                    _uiState.value = MergeUiState.Success(mergedFile)  
                    _selectedFiles.value = emptyList() // Clear list after successful merge  
                }  
                .onFailure { exception ->  
                    _uiState.value = MergeUiState.Error(exception.localizedMessage ?: "Merge failed.")  
                }  
        }  
    }  
}  
  
sealed interface MergeUiState {  
    object Idle : MergeUiState  
    object Loading : MergeUiState  
    data class Success(val file: File) : MergeUiState  
    data class Error(val message: String) : MergeUiState  
}