package com.zenlock.auth.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zenlock.auth.data.repository.OtpRepository

class OtpRefreshWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: OtpRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {

            // 🔄 Trigger OTP regeneration for all accounts
            repository.refreshAllOtps()

            Result.success()

        } catch (e: Exception) {
            Result.retry()
        }
    }
}