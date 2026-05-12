package com.aegis.pdf.features.convert

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfficeToPdfConverter @Inject constructor(
    private val engine: ConvertEngine
) {
    suspend fun wordToPdf(input: Uri) = engine.convert(input, ConvertType.WORD_TO_PDF)
    suspend fun excelToPdf(input: Uri) = engine.convert(input, ConvertType.EXCEL_TO_PDF)
    suspend fun pptToPdf(input: Uri) = engine.convert(input, ConvertType.PPT_TO_PDF)
}