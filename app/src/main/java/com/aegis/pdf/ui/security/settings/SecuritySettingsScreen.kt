package com.aegis.pdf.ui.security

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SecuritySettingsScreen(onBack: () -> Unit) {
    var biometricEnabled by remember { mutableStateOf(true) }
    var autoLockTimeout by remember { mutableStateOf("5") }
    var clearOnExit by remember { mutableStateOf(false) }
    var screenCaptureBlock by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Security Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingRow("Biometric Lock", "Require fingerprint to open app") {
                    Switch(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it })
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingRow("Screen Capture Block", "Prevent screenshots") {
                    Switch(checked = screenCaptureBlock, onCheckedChange = { screenCaptureBlock = it })
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingRow("Clear on Exit", "Auto-delete files when app closes") {
                    Switch(checked = clearOnExit, onCheckedChange = { clearOnExit = it })
                }
            }
        }
    }
}

@Composable
fun SettingRow(
    title: String,
    description: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        trailing()
    }
}