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
import kotlinx.coroutines.delay

@Composable
fun OtpListScreen(
    viewModel: OtpViewModel = viewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val otpMap by viewModel.otpCodes.collectAsState()

    // Auto refresh every 1 second (UI timer loop)
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refreshOtps()
            delay(1000L)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Zenlock Authenticator",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

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