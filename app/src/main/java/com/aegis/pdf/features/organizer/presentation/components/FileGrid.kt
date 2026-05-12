package com.aegis.pdf.features.organizer.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aegis.pdf.features.organizer.data.local.entity.FolderEntity
import com.aegis.pdf.features.organizer.data.local.entity.RecentFileEntity

sealed class FileGridItem {
    data class FolderItem(val folder: FolderEntity) : FileGridItem()
    data class FileItem(val file: RecentFileEntity) : FileGridItem()
}

@Composable
fun FileGrid(
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
    val allItems = mutableListOf<FileGridItem>()
    allItems.addAll(folders.map { FileGridItem.FolderItem(it) })
    allItems.addAll(files.map { FileGridItem.FileItem(it) })

    if (allItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
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
                    "This folder is empty",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            allItems,
            key = { item ->
                when (item) {
                    is FileGridItem.FolderItem -> "folder_${item.folder.id}"
                    is FileGridItem.FileItem -> "file_${item.file.id}"
                }
            }
        ) { item ->
            when (item) {
                is FileGridItem.FolderItem -> {
                    FolderGridCard(
                        folder = item.folder,
                        isSelected = item.folder.id in selectedFolders,
                        onClick = { onFolderClick(item.folder.id) },
                        onLongClick = { onFolderLongClick(item.folder.id) }
                    )
                }
                is FileGridItem.FileItem -> {
                    FileGridCard(
                        file = item.file,
                        isSelected = item.file.id in selectedFiles,
                        onClick = { onFileClick(item.file.id) },
                        onLongClick = { onFileLongClick(item.file.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderGridCard(
    folder: FolderEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = "Folder",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                folder.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun FileGridCard(
    file: RecentFileEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.PictureAsPdf,
                contentDescription = "PDF",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            Text(
                file.fileName,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                formatSize(file.fileSize),
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}