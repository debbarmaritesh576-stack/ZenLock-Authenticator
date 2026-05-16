package com.aegis.pdf.core.worker  
  
import android.content.Context  
import androidx.hilt.work.HiltWorker  
import androidx.work.CoroutineWorker  
import androidx.work.WorkerParameters  
import androidx.work.workDataOf  
import com.aegis.pdf.core.pdf.PdfMergerEngine  
import dagger.assisted.Assisted  
import dagger.assisted.AssistedInject  
import java.io.File  
  
@HiltWorker  
class PdfMergerWorker @AssistedInject constructor(  
    @Assisted private val context: Context,  
    @Assisted params: WorkerParameters,  
    private val mergerEngine: PdfMergerEngine  
) : CoroutineWorker(context, params) {  
  
    override suspend fun doWork(): Result {  
        // Input arguments uthao (Files paths aur output name)  
        val inputPaths = inputData.getStringArray("KEY_INPUT_PATHS") ?: return Result.failure()  
        val outputName = inputData.getString("KEY_OUTPUT_NAME") ?: "Aegis_Merged_Doc"  
          
        val inputFiles = inputPaths.map { File(it) }  
        val outputDir = File(context.filesDir, "merged_outputs").apply { mkdirs() }  
        val outputFile = File(outputDir, if (outputName.endsWith(".pdf")) outputName else "$outputName.pdf")  
  
        // Progress update notification trigger ke liye yahan handle kar sakte ho  
        setProgress(workDataOf("KEY_PROGRESS" to "Merging started..."))  
  
        val result = mergerEngine.mergeDocuments(inputFiles, outputFile)  
  
        return if (result.isSuccess) {  
            val successData = workDataOf("KEY_OUTPUT_PATH" to outputFile.absolutePath)  
            Result.success(successData)  
        } else {  
            val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Unknown Merge Error"  
            Result.failure(workDataOf("KEY_ERROR" to errorMsg))  
        }  
    }  
}