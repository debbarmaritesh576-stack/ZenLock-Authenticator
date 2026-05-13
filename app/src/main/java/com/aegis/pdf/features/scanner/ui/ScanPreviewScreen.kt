package com.aegis.pdf.features.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.features.scanner.viewmodel.ScannerViewModel
import com.aegis.pdf.features.scanner.data.ScanRepository
import android.graphics.Bitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPreviewScreen(
    scans: List<Bitmap>,
    onCreatePdf: () -> Unit,
    onAddMoreScans: () -> Unit,
    onBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
    scanRepository: ScanRepository = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var selectedIndex by remember { mutableStateOf(-1) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteIndex by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview Scans (${scans.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(scans.size) { index ->
                    PageThumbnail(
                        bitmap = scans[index],
                        pageNumber = index + 1,
                        isSelected = selectedIndex == index,
                        onSelect = { selectedIndex = index },
                        onDelete = {
                            deleteIndex = index
                            showDeleteConfirm = true
                        },
                        onRotate = { viewModel.rotateScan(index, 90) }
                    )
                }
            }

            if (selectedIndex != -1 && selectedIndex < scans.size) {
                PageToolbar(
                    pageIndex = selectedIndex,
                    onRotate = { viewModel.rotateScan(selectedIndex, 90) },
                    onDelete = {
                        deleteIndex = selectedIndex
                        showDeleteConfirm = true
                    },
                    onMoveUp = {
                        if (selectedIndex > 0) {
                            viewModel.reorderScans(selectedIndex, selectedIndex - 1)
                            selectedIndex--
                        }
                    },
                    onMoveDown = {
                        if (selectedIndex < scans.size - 1) {
                            viewModel.reorderScans(selectedIndex, selectedIndex + 1)
                            selectedIndex++
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddMoreScans,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Scan")
                }

                Button(
                    onClick = onCreatePdf,
                    modifier = Modifier.weight(1f),
                    enabled = scans.isNotEmpty()
                ) {
                    Icon(Icons.Default.FileCopy, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Create PDF")
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Scan?") },
                text = { Text("Are you sure you want to delete page ${deleteIndex + 1}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.removeScan(deleteIndex)
                        showDeleteConfirm = false
                        selectedIndex = -1
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PageThumbnail(
    bitmap: Bitmap,
    pageNumber: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onRotate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(100.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else Color.Transparent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.LightGray, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
        ) {
            AndroidThumbnail(bitmap = bitmap, modifier = Modifier.fillMaxSize())
        }

        Text(
            "Page $pageNumber",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp)
        )
    }
}

@Composable
fun AndroidThumbnail(bitmap: Bitmap, modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            android.widget.ImageView(context).apply {
                setImageBitmap(bitmap)
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            }
        },
        modifier = modifier
    )
}

@Composable
fun PageToolbar(
    pageIndex: Int,
    onRotate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onMoveUp,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(Icons.Default.ArrowUpward, "Move Up")
        }

        IconButton(
            onClick = onRotate,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(Icons.Default.RotateRight, "Rotate")
        }

        IconButton(
            onClick = onMoveDown,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(Icons.Default.ArrowDownward, "Move Down")
        }

        Spacer(Modifier.weight(1f))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
        ) {
            Icon(Icons.Default.Delete, "Delete")
        }
    }
}