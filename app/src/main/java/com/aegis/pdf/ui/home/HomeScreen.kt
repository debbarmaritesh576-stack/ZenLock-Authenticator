package com.aegis.pdf.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.ui.components.ToolCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onToolClick: (HomeViewModel.Tool) -> Unit = {}
) {
    val recentFiles by viewModel.recentFiles.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Aegis PDF",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "All-in-One PDF Solution",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (recentFiles.isNotEmpty()) {
            item { Text("Recent Files", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
        }

        item { Text("Tools", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }

        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.tools) { tool ->
                    ToolCard(
                        name = tool.name,
                        description = tool.description,
                        onClick = { onToolClick(tool) }
                    )
                }
            }
        }
    }
}