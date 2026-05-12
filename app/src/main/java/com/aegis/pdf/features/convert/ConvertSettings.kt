package com.aegis.pdf.features.convert

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ConvertPrefs(val imageQuality: Int = 90, val imageFormat: String = "PNG", val preserveFormatting: Boolean = true)

@Singleton
class ConvertSettings @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("convert_prefs", Context.MODE_PRIVATE)
    fun get(): ConvertPrefs = ConvertPrefs(
        imageQuality = prefs.getInt("quality", 90),
        imageFormat = prefs.getString("format", "PNG") ?: "PNG",
        preserveFormatting = prefs.getBoolean("preserve", true)
    )
    fun save(p: ConvertPrefs) { prefs.edit().putInt("quality", p.imageQuality).putString("format", p.imageFormat).putBoolean("preserve", p.preserveFormatting).apply() }
}

@Composable
fun ConvertSettingsScreen(onBack: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text("Conversion Settings", style = MaterialTheme.typography.titleMedium)
        var quality by remember { mutableStateOf(90) }
        Text("Image Quality: $quality%")
        Slider(value = quality.toFloat(), onValueChange = { quality = it.toInt() }, valueRange = 10f..100f)
        Button(onClick = onBack) { Text("Save") }
    }
}