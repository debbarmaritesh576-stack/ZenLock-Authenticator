package com.aegis.pdf.features.convert

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageToPdfConverter @Inject constructor(
    private val engine: ConvertEngine
) {
    suspend fun convert(images: List<Uri>) = engine.convertImagesToPdf(images, "")
}