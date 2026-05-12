package com.aegis.pdf.features.convert

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfToImageConverter @Inject constructor(
    private val engine: ConvertEngine
) {
    suspend fun convert(input: Uri, format: String = "PNG") = engine.convert(input, ConvertType.PDF_TO_IMAGE)
}