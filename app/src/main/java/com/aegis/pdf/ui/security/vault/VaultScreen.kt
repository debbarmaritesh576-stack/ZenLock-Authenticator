package com.aegis.pdf.ui.security

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.pdf.core.security.VaultFile

@Composable
fun VaultScreen(
    viewModel: VaultViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vaultFiles by viewModel.vaultFiles.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addToVault(it, context) }
    }

    if (isLocked) {
        BiometricLockScreen(
            onAuthenticated = { viewModel.unlock() },
            onCancel = onBack
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Secure Vault", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = { viewModel.lock() }) {
                    Icon(Icons.Default.Lock, "Lock Vault")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { launcher.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add PDF to Vault")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (vaultFiles.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Security, null, modifier = Modifier.size(48.dp))
                            Text("Vault is empty")
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vaultFiles) { file ->
                        VaultFileCard(
                            file = file,
                            onRemove = { viewModel.removeFromVault(file) },
                            onExport = { viewModel.exportFile(file, context) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VaultFileCard(
    file: VaultFile,
    onRemove: () -> Unit,
    onExport: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, fontWeight = FontWeight.SemiBold)
                Text(
                    formatSize(file.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onExport) { Icon(Icons.Default.FileDownload, "Export") }
            IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, "Remove") }
        }
    }
}

fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}