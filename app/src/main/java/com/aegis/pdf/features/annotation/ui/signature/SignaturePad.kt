package com.aegis.pdf.features.annotation.ui.signature

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import android.graphics.PointF

@Composable
fun SignaturePad(
    onSignatureCapture: (String) -> Unit,
    onCancel: () -> Unit
) {
    val signaturePoints = remember { mutableStateListOf<PointF>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Sign here", style = MaterialTheme.typography.labelSmall)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            signaturePoints.add(PointF(offset.x, offset.y))
                        },
                        onDrag = { change, _ ->
                            signaturePoints.add(PointF(change.position.x, change.position.y))
                        }
                    )
                }
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { signaturePoints.clear() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Clear")
            }

            Button(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }

            Button(
                onClick = { onSignatureCapture("signature_${System.currentTimeMillis()}") },
                modifier = Modifier.weight(1f),
                enabled = signaturePoints.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
}