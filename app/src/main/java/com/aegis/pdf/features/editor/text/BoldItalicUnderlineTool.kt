package com.aegis.pdf.features.editor.text

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BoldItalicUnderlineTool(
    bold: Boolean,
    italic: Boolean,
    underline: Boolean,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilledTonalButton(
            onClick = onBoldClick,
            modifier = Modifier.width(44.dp),
            colors = if (bold) 
                ButtonDefaults.filledTonalButtonColors()
            else
                ButtonDefaults.outlinedButtonColors()
        ) {
            Text("B", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        FilledTonalButton(
            onClick = onItalicClick,
            modifier = Modifier.width(44.dp),
            colors = if (italic)
                ButtonDefaults.filledTonalButtonColors()
            else
                ButtonDefaults.outlinedButtonColors()
        ) {
            Text("I", fontStyle = FontStyle.Italic, fontSize = 16.sp)
        }

        FilledTonalButton(
            onClick = onUnderlineClick,
            modifier = Modifier.width(44.dp),
            colors = if (underline)
                ButtonDefaults.filledTonalButtonColors()
            else
                ButtonDefaults.outlinedButtonColors()
        ) {
            Text("U", textDecoration = TextDecoration.Underline, fontSize = 16.sp)
        }
    }
}