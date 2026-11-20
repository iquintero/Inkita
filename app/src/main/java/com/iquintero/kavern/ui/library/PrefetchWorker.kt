package com.iquintero.kavern.ui.library

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.iquintero.kavern.core.logging.LoggingManager

class PrefetchWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private val tag = "PrefetchWorker"

    override suspend fun doWork(): Result {
        LoggingManager.d(tag, "Prefetch worker skeleton: no-op")
        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            LoggingManager.d("PrefetchWorker", "enqueue called (skeleton)")
            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            val work =
                OneTimeWorkRequestBuilder<PrefetchWorker>()
                    .setConstraints(constraints)
                    .build()
            LoggingManager.d("PrefetchWorker", "enqueueUniqueWork kavern_prefetch")
            WorkManager.getInstance(context).enqueueUniqueWork("kavern_prefetch", ExistingWorkPolicy.REPLACE, work)
        }
    }
}
// Prefetch logic will be reimplemented later.
