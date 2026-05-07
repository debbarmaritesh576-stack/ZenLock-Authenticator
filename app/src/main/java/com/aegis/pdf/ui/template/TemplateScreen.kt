package com.aegis.pdf.ui.template

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val templates = listOf(
        TemplateItem("Invoice", "Professional invoice", Icons.Default.Receipt),
        TemplateItem("Resume", "Job application", Icons.Default.Person),
        TemplateItem("Letter", "Formal letter", Icons.Default.Mail),
        TemplateItem("Blank", "Empty page", Icons.Default.InsertDriveFile),
        TemplateItem("Lined", "Ruled pages", Icons.Default.ViewList),
        TemplateItem("Grid", "Graph paper", Icons.Default.GridOn)
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("PDF Templates", style = MaterialTheme.typography.headlineMedium)
        Text("Create professional documents quickly", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    onClick = { viewModel.generateTemplate(template.type) }
                )
            }
        }
    }
}

data class TemplateItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val type: String = name.lowercase()
)

@Composable
fun TemplateCard(
    template: TemplateItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(template.icon, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Text(template.name, fontWeight = FontWeight.SemiBold)
            Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}