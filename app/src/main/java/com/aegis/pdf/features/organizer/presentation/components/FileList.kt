package com.aegis.pdf.features.organizer.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aegis.pdf.features.organizer.data.local.entity.FolderEntity
import com.aegis.pdf.features.organizer.data.local.entity.RecentFileEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileList(
    folders: List<FolderEntity>,
    files: List<RecentFileEntity>,
    onFolderClick: (String) -> Unit,
    onFileClick: (String) -> Unit,
    onFolderLongClick: (String) -> Unit = {},
    onFileLongClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    selectedFolders: Set<String> = emptySet(),
    selectedFiles: Set<String> = emptySet()
) {
    LazyColumn(modifier = modifier) {
        if (folders.isNotEmpty()) {
            items(
                folders,
                key = { folder -> "folder_${folder.id}" }
            ) { folder ->
                FolderListItem(
                    folder = folder,
                    isSelected = folder.id in selectedFolders,
                    onClick = { onFolderClick(folder.id) },
                    onLongClick = { onFolderLongClick(folder.id) }
                )
                HorizontalDivider()
            }

            item {
                Spacer(Modifier.height(8.dp))
            }
        }

        items(
            files,
            key = { file -> "file_${file.id}" }
        ) { file ->
            FileListItem(
                file = file,
                isSelected = file.id in selectedFiles,
                onClick = { onFileClick(file.id) },
                onLongClick = { onFileLongClick(file.id) }
            )
            if (file != files.lastOrNull()) {
                HorizontalDivider()
            }
        }

        if (folders.isEmpty() && files.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No files or folders",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderListItem(
    folder: FolderEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    ListItem(
        headlineContent = {
            Text(
                folder.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                "Folder",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    file: RecentFileEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    ListItem(
        headlineContent = {
            Text(
                file.fileName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatSize(file.fileSize),
                    style = MaterialTheme.typography.bodySmall
                )
                Text("·", style = MaterialTheme.typography.bodySmall)
                Text(
                    formatDate(file.lastOpened),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        leadingContent = {
            Icon(
                Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        trailingContent = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, "Options")
            }
        },
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    )
}