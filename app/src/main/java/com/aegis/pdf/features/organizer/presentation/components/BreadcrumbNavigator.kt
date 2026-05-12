package com.aegis.pdf.features.organizer.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.pdf.features.organizer.presentation.model.BreadcrumbUiModel

@Composable
fun BreadcrumbNavigator(
    breadcrumbs: List<BreadcrumbUiModel>,
    onBreadcrumbClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onBreadcrumbClick(null) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier.size(18.dp)
            )
        }

        breadcrumbs.forEachIndexed { index, crumb ->
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val isLast = index == breadcrumbs.size - 1
            Text(
                text = crumb.name,
                modifier = Modifier
                    .clickable(enabled = !isLast) { onBreadcrumbClick(crumb.id) }
                    .padding(horizontal = 4.dp),
                style = if (isLast) MaterialTheme.typography.labelLarge
                else MaterialTheme.typography.bodyMedium,
                color = if (isLast) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}