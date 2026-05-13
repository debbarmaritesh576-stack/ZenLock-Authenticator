package com.aegis.pdf.features.annotation.ui.stamp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.features.annotation.viewmodel.AnnotationViewModel
import com.aegis.pdf.features.annotation.model.AnnotationType

enum class StampType {
    APPROVED, REJECTED, DRAFT, CONFIDENTIAL, FOR_REVIEW
}

@Composable
fun StampPicker(
    onStampSelected: (StampType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Select Stamp", style = MaterialTheme.typography.titleSmall)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StampTypeButton(
                label = "Approved",
                color = Color.Green,
                onClick = { onStampSelected(StampType.APPROVED) },
                modifier = Modifier.weight(1f)
            )

            StampTypeButton(
                label = "Rejected",
                color = Color.Red,
                onClick = { onStampSelected(StampType.REJECTED) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StampTypeButton(
                label = "Draft",
                color = Color.Yellow,
                onClick = { onStampSelected(StampType.DRAFT) },
                modifier = Modifier.weight(1f)
            )

            StampTypeButton(
                label = "Review",
                color = Color.Blue,
                onClick = { onStampSelected(StampType.FOR_REVIEW) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StampTypeButton(
                label = "Confidential",
                color = Color.Magenta,
                onClick = { onStampSelected(StampType.CONFIDENTIAL) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StampTypeButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, color = Color.White)
    }
}