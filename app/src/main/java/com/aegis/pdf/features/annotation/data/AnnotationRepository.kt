package com.aegis.pdf.features.annotation.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import com.aegis.pdf.features.annotation.model.Annotation

@Singleton
class AnnotationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "AnnotationRepository"
    private val annotationDir = File(context.filesDir, "annotations")

    init {
        annotationDir.mkdirs()
    }

    suspend fun saveAnnotations(pageNumber: Int, annotations: List<Annotation>): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(annotationDir, "page_$pageNumber.json")
            val json = Gson().toJson(annotations)
            file.writeText(json)
            Log.d(TAG, "Annotations saved for page $pageNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save annotations", e)
            false
        }
    }

    suspend fun loadAnnotations(pageNumber: Int): List<Annotation> = withContext(Dispatchers.IO) {
        try {
            val file = File(annotationDir, "page_$pageNumber.json")
            if (!file.exists()) return@withContext emptyList()

            val json = file.readText()
            val type = object : com.google.gson.reflect.TypeToken<List<Annotation>>() {}.type
            val annotations = Gson().fromJson<List<Annotation>>(json, type) ?: emptyList()
            Log.d(TAG, "Annotations loaded for page $pageNumber: ${annotations.size}")
            annotations
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load annotations", e)
            emptyList()
        }
    }

    suspend fun deleteAnnotations(pageNumber: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(annotationDir, "page_$pageNumber.json")
            file.delete()
            Log.d(TAG, "Annotations deleted for page $pageNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete annotations", e)
            false
        }
    }

    suspend fun exportAllAnnotations(): String = withContext(Dispatchers.IO) {
        try {
            val allAnnotations = mutableListOf<Annotation>()
            annotationDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".json")) {
                    val json = file.readText()
                    val type = object : com.google.gson.reflect.TypeToken<List<Annotation>>() {}.type
                    val annotations = Gson().fromJson<List<Annotation>>(json, type)
                    allAnnotations.addAll(annotations)
                }
            }
            Gson().toJson(allAnnotations)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export annotations", e)
            ""
        }
    }
}