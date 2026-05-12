package com.aegis.pdf.features.convert

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfToOfficeConverter @Inject constructor(
    private val engine: ConvertEngine
) {
    suspend fun toWord(input: Uri) = engine.convert(input, ConvertType.PDF_TO_WORD)
    suspend fun toExcel(input: Uri) = engine.convert(input, ConvertType.PDF_TO_EXCEL)
    suspend fun toPowerPoint(input: Uri) = engine.convert(input, ConvertType.PDF_TO_PPT)
}