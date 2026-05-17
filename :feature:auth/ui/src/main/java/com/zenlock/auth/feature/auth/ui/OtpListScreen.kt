package com.zenlock.auth.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenlock.auth.data.local.entity.AuthAccountEntity
import com.zenlock.auth.feature.auth.viewmodel.OtpViewModel

@Composable
fun OtpListScreen(
    onAddAccount: () -> Unit,
    viewModel: OtpViewModel = viewModel()
) {

    val accounts by viewModel.accounts.collectAsState()
    val otpMap by viewModel.otpCodes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ZenLock Authenticator",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(onClick = onAddAccount) {
                Text("+ Add")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No accounts added yet")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(accounts) { account ->
                    OtpCard(
                        account = account,
                        otp = otpMap[account.id] ?: "------"
                    )
                }
            }
        }
    }
}