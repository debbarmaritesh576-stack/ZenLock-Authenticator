package com.aegis.pdf.core.worker  
  
import android.content.Context  
import androidx.hilt.work.HiltWorker  
import androidx.work.CoroutineWorker  
import androidx.work.WorkerParameters  
import dagger.assisted.Assisted  
import dagger.assisted.AssistedInject  
  
@HiltWorker  
class ConversionHistoryWorker @AssistedInject constructor(  
    @Assisted private val context: Context,  
    @Assisted params: WorkerParameters  
) : CoroutineWorker(context, params) {  
    override suspend fun doWork(): Result {  
        // Automation clean handler for temp scratch HTML images data elements caches  
        return Result.success()  
    }  
}