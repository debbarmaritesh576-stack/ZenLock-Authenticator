package com.aegis.pdf.features.convert

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConvertHistory @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("convert_hist", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val maxRecords = 50

    fun add(type: String, path: String) {
        val list = getAll().toMutableList()
        list.add(0, ConvertRecord(type, path))
        if (list.size > maxRecords) list.removeAt(list.size - 1)
        prefs.edit().putString("records", gson.toJson(list)).apply()
    }

    fun getAll(): List<ConvertRecord> = try {
        gson.fromJson(prefs.getString("records", "[]"), object : TypeToken<List<ConvertRecord>>() {}.type)
    } catch (e: Exception) { emptyList() }

    fun clear() { prefs.edit().remove("records").apply() }
}

@Composable
fun ConvertHistoryScreen(history: List<ConvertRecord>, onClear: () -> Unit) {
    LazyColumn(Modifier.padding(16.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("History"); TextButton(onClick = onClear) { Text("Clear") } } }
        items(history) { Text("${it.type} → ${it.path.takeLast(30)}", modifier = Modifier.padding(vertical = 4.dp)) }
    }
}