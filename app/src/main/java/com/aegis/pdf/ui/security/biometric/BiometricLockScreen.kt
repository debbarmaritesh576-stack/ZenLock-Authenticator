package com.aegis.pdf.ui.security

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.aegis.pdf.core.security.BiometricLockManager

@Composable
fun BiometricLockScreen(
    onAuthenticated: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val biometricManager = remember { BiometricLockManager(context) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val result = biometricManager.authenticate(activity)
            if (result) onAuthenticated()
            else errorMessage = "Authentication failed"
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                "Verify Identity",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                "Use fingerprint or face to unlock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            TextButton(onClick = {
                onAuthenticated() // Skip biometrics
            }) {
                Text("Use Password Instead")
            }
        }
    }
}