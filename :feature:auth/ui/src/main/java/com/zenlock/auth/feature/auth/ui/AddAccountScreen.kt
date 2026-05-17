package com.zenlock.auth.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenlock.auth.feature.auth.viewmodel.OtpViewModel

@Composable
fun AddAccountScreen(
    onOpenQrScanner: () -> Unit,
    onAccountAdded: () -> Unit,
    viewModel: OtpViewModel = viewModel()
) {

    var issuer by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Add Authenticator Account",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = issuer,
            onValueChange = { issuer = it },
            label = { Text("Issuer (Google, GitHub etc.)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text("Account Name / Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = secret,
            onValueChange = { secret = it },
            label = { Text("Secret Key (Base32)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onOpenQrScanner,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan QR Code")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.addAccount(
                    issuer = issuer,
                    accountName = accountName,
                    secret = secret
                )
                onAccountAdded()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Account")
        }
    }
}