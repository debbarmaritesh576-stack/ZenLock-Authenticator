package com.aegis.pdf.features.editor.text  
  
import androidx.compose.foundation.layout.*  
import androidx.compose.material3.*  
import androidx.compose.runtime.*  
import androidx.compose.ui.Modifier  
import androidx.compose.ui.unit.dp  
  
@Composable  
fun ParagraphEditor(  
    element: TextElement?,  
    onIndentChange: (Float) -> Unit,  
    onMarginChange: (Float, Float, Float, Float) -> Unit,  
    onBulletToggle: (Boolean) -> Unit,  
    onNumberingToggle: (Boolean) -> Unit,  
    modifier: Modifier = Modifier  
) {  
    if (element == null) return  
  
    var leftIndent by remember { mutableStateOf(0f) }  
    var rightIndent by remember { mutableStateOf(0f) }  
    var topMargin by remember { mutableStateOf(0f) }  
    var bottomMargin by remember { mutableStateOf(0f) }  
    var hasBullet by remember { mutableStateOf(false) }  
    var hasNumbering by remember { mutableStateOf(false) }  
  
    Column(modifier = modifier.fillMaxWidth().padding(12.dp)) {  
        Text("Paragraph Settings", style = MaterialTheme.typography.titleSmall)  
        Spacer(modifier = Modifier.height(12.dp))  
  
        Text("Left Indent: ${leftIndent.toInt()}pt")  
        Slider(value = leftIndent, onValueChange = { leftIndent = it; onIndentChange(it) }, valueRange = 0f..100f)  
  
        Text("Right Indent: ${rightIndent.toInt()}pt")  
        Slider(value = rightIndent, onValueChange = { rightIndent = it }, valueRange = 0f..100f)  
  
        Text("Top Margin: ${topMargin.toInt()}pt")  
        Slider(value = topMargin, onValueChange = { topMargin = it }, valueRange = 0f..50f)  
  
        Text("Bottom Margin: ${bottomMargin.toInt()}pt")  
        Slider(value = bottomMargin, onValueChange = { bottomMargin = it; onMarginChange(leftIndent, rightIndent, topMargin, bottomMargin) }, valueRange = 0f..50f)  
  
        Spacer(modifier = Modifier.height(8.dp))  
  
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {  
            FilterChip(selected = hasBullet, onClick = { hasBullet = !hasBullet; onBulletToggle(hasBullet) }, label = { Text("• Bullets") })  
            FilterChip(selected = hasNumbering, onClick = { hasNumbering = !hasNumbering; onNumberingToggle(hasNumbering) }, label = { Text("1. Numbering") })  
        }  
    }  
}