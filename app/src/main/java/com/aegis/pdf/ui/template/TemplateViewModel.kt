package com.aegis.pdf.ui.template

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.pdf.TemplateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val templateManager: TemplateManager
) : ViewModel() {

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    fun generateTemplate(type: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val templateType = when (type.lowercase()) {
                    "invoice" -> TemplateManager.TemplateType.INVOICE
                    "resume" -> TemplateManager.TemplateType.RESUME
                    "letter" -> TemplateManager.TemplateType.LETTER
                    "blank" -> TemplateManager.TemplateType.BLANK
                    "lined" -> TemplateManager.TemplateType.LINED
                    "grid" -> TemplateManager.TemplateType.GRID
                    else -> TemplateManager.TemplateType.BLANK
                }
                
                val outputFile = File(context.filesDir, "templates/${type}_${System.currentTimeMillis()}.pdf")
                outputFile.parentFile?.mkdirs()
                
                val success = templateManager.generatePdf(templateType, outputFile)
                withContext(Dispatchers.Main) {
                    _resultMessage.value = if (success) "Template created: ${outputFile.name}"
                    else "Failed to create template"
                }
            }
        }
    }
}