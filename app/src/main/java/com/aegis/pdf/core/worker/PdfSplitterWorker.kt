package com.aegis.pdf.core.worker  
  
import android.content.Context  
import androidx.hilt.work.HiltWorker  
import androidx.work.CoroutineWorker  
import androidx.work.WorkerParameters  
import androidx.work.workDataOf  
import com.aegis.pdf.core.pdf.PdfSplitter  
import dagger.assisted.Assisted  
import dagger.assisted.AssistedInject  
import java.io.File  
  
@HiltWorker  
class PdfSplitterWorker @AssistedInject constructor(  
    @Assisted private val context: Context,  
    @Assisted params: WorkerParameters,  
    private val splitter: PdfSplitter  
) : CoroutineWorker(context, params) {  
  
    override suspend fun doWork(): Result {  
        val srcPath = inputData.getString("KEY_SRC_PATH") ?: return Result.failure()  
        val targetName = inputData.getString("KEY_TARGET_NAME") ?: "Split_Output"  
        val pageArray = inputData.getIntArray("KEY_PAGES_LIST") ?: return Result.failure()  
  
        val sourceFile = File(srcPath)  
        val outputDir = File(context.filesDir, "split_outputs").apply { mkdirs() }  
        val outputFile = File(outputDir, "$targetName.pdf")  
  
        val result = splitter.splitSpecificPages(sourceFile, pageArray.toList(), outputFile)  
  
        return if (result.isSuccess) {  
            Result.success(workDataOf("KEY_RESULT_PATH" to outputFile.absolutePath))  
        } else {  
            Result.failure(workDataOf("KEY_LOG_ERR" to (result.exceptionOrNull()?.message ?: "Slicing Failure")))  
        }  
    }  
}