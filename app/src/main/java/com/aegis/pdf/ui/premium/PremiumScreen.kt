package com.aegis.pdf.ui.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val products by viewModel.products.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF1473E6), Color(0xFF7B2FBE))
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Aegis Premium",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Unlock the full power",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        if (isPremium) {
            PremiumActiveView()
        } else {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Choose Your Plan",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            PlanCard(
                title = "Monthly",
                price = "$4.99",
                period = "/month",
                color = Color(0xFF1473E6),
                features = listOf("All premium features", "Cancel anytime"),
                onSelect = { viewModel.subscribe("monthly") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PlanCard(
                title = "Yearly",
                price = "$29.99",
                period = "/year",
                color = Color(0xFF7B2FBE),
                features = listOf("Save 50%", "All premium features", "Priority support"),
                popular = true,
                onSelect = { viewModel.subscribe("yearly") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PlanCard(
                title = "Lifetime",
                price = "$59.99",
                period = "once",
                color = Color(0xFFFF6B35),
                features = listOf("Pay once, forever", "All premium features", "Lifetime updates"),
                onSelect = { viewModel.subscribe("lifetime") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Purchases")
            }
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    period: String,
    color: Color,
    features: List<String>,
    popular: Boolean = false,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = if (popular) {
            androidx.compose.foundation.BorderStroke(2.dp, color)
        } else null
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (popular) {
                Surface(
                    color = color,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "MOST POPULAR",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.End) {
                    Text(price, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
                    Text(period, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feature, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get Started")
            }
        }
    }
}

@Composable
fun PremiumActiveView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Verified, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("You're Premium!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Enjoy all premium features", fontSize = 14.sp, color = Color.Gray)
        }
    }
}