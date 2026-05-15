package com.aegis.pdf.core.ocr  
  
import android.content.Context  
import androidx.work.CoroutineWorker  
import androidx.work.WorkerParameters  
import com.aegis.pdf.data.local.db.OcrSearchDao  
import com.aegis.pdf.data.local.db.OcrSearchEntity  
import java.io.File  
  
class OcrIndexingWorker(  
    context: Context,  
    params: WorkerParameters,  
    private val ocrProcessor: OcrProcessor,  
    private val ocrSearchDao: OcrSearchDao  
) : CoroutineWorker(context, params) {  
  
    override suspend fun doWork(): Result {  
        val filePath = inputData.getString("FILE_PATH") ?: return Result.failure()  
        val fileId = inputData.getString("FILE_ID") ?: return Result.failure()  
        val file = File(filePath)  
  
        return try {  
            val ocrResult = ocrProcessor.processImageFile(file)  
            if (ocrResult is OcrResult.Success) {  
                // FTS5 table mein data insert karo taaki search mein dikhne lage  
                ocrSearchDao.insertContent(  
                    OcrSearchEntity(  
                        fileId = fileId,  
                        fileName = file.name,  
                        extractedText = ocrResult.text  
                    )  
                )  
                Result.success()  
            } else {  
                Result.retry()  
            }  
        } catch (e: Exception) {  
            Result.failure()  
        }  
    }  
}