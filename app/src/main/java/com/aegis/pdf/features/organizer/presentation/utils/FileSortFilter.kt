package com.aegis.pdf.features.organizer.presentation.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.pdf.features.organizer.presentation.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSortFilter(
    currentSort: SortOption,
    isAscending: Boolean,
    onSortChange: (SortOption) -> Unit,
    onDirectionToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Sort by",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortOption.values().forEach { option ->
                FilterChip(
                    selected = currentSort == option,
                    onClick = { onSortChange(option) },
                    label = { Text(option.name) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Direction",
                style = MaterialTheme.typography.bodyMedium
            )

            FilledTonalButton(
                onClick = onDirectionToggle,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(if (isAscending) "↑ Ascending" else "↓ Descending")
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}