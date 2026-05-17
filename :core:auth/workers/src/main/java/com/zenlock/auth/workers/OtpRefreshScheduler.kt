package com.zenlock.auth.workers

import android.content.Context
import androidx.work.*

import java.util.concurrent.TimeUnit

object OtpRefreshScheduler {

    fun startPeriodicRefresh(context: Context) {

        val request = PeriodicWorkRequestBuilder<OtpRefreshWorker>(
            15, TimeUnit.MINUTES // WorkManager minimum allowed interval
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "otp_refresh_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}