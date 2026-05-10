package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorScreen(
    onBack: () -> Unit,
    viewModel: TextEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text Editor") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Done") }
                },
                actions = {
                    TextButton(onClick = { viewModel.undo() }) { Text("Undo") }
                    TextButton(onClick = { viewModel.redo() }) { Text("Redo") }
                }
            )
        },
        bottomBar = {
            TextEditorToolbar(
                currentTool = state.selectedTool,
                onToolSelected = { viewModel.selectTool(it) },
                currentFont = state.currentFont,
                currentSize = state.currentFontSize,
                currentColor = state.currentColor,
                onFontClick = { viewModel.showFontPicker() },
                onSizeClick = { viewModel.showSizeSlider() },
                onColorClick = { viewModel.showColorPicker() },
                bold = state.isBold,
                italic = state.isItalic,
                underline = state.isUnderline,
                onBoldClick = { viewModel.toggleBold() },
                onItalicClick = { viewModel.toggleItalic() },
                onUnderlineClick = { viewModel.toggleUnderline() },
                alignment = state.alignment,
                onAlignmentClick = { viewModel.cycleAlignment() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            TextEditorCanvas(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onTextTap = { viewModel.selectText(it) },
                onTextMove = { id, x, y -> viewModel.moveText(id, x, y) },
                onTextResize = { id, w, h -> viewModel.resizeText(id, w, h) },
                onCanvasTap = { x, y -> viewModel.addTextBox(x, y) }
            )
        }
    }
    
    // Dialogs
    if (state.showFontPicker) {
        FontPickerDialog(
            currentFont = state.currentFont,
            onFontSelected = { viewModel.selectFont(it) },
            onDismiss = { viewModel.hideFontPicker() }
        )
    }
    
    if (state.showSizeSlider) {
        FontSizeSlider(
            currentSize = state.currentFontSize,
            onSizeChanged = { viewModel.setFontSize(it) },
            onDismiss = { viewModel.hideSizeSlider() }
        )
    }
    
    if (state.showColorPicker) {
        ColorPickerDialog(
            currentColor = state.currentColor,
            onColorSelected = { viewModel.setColor(it) },
            onDismiss = { viewModel.hideColorPicker() }
        )
    }
}