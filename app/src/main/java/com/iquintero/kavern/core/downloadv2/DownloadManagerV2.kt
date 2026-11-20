package com.iquintero.kavern.core.downloadv2

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.iquintero.kavern.core.logging.LoggingManager
import com.iquintero.kavern.core.network.NetworkMonitor
import com.iquintero.kavern.core.storage.AppPreferences
import com.iquintero.kavern.core.storage.KavitaProfile
import kotlinx.coroutines.flow.first

/**
 * Download V2 orchestrator that enqueues jobs and schedules a worker with proper constraints.
 */
class DownloadManagerV2(
    private val appPreferences: AppPreferences,
    private val strategies: Map<String, DownloadStrategyV2>,
) {
    /**
     * Enqueue a request through the matching strategy and schedule the worker.
     * Returns the created job id or -1 if no strategy matches.
     */
    suspend fun enqueue(request: DownloadRequestV2): Long {
        val strategy = strategies[request.format] ?: return -1L
        val jobId = strategy.enqueue(request)
        if (LoggingManager.isDebugEnabled()) {
            LoggingManager.d(
                "DownloadManagerV2",
                "Enqueued job=$jobId type=${request.type} fmt=${request.format} series=${request.seriesId} " +
                    "volume=${request.volumeId} chapter=${request.chapterId} page=${request.pageIndex} count=${request.pageCount}",
            )
        }
        if (jobId > 0) enqueueWorker(appPreferences)
        return jobId
    }

    companion object {
        private const val WORK_NAME = "download_v2_worker"

        suspend fun enqueueWorker(prefs: AppPreferences) {
            val profile = prefs.profile
            if (profile == KavitaProfile.Guest || prefs.offlineModeFlow.first()) return
            val context = prefs.appContext
            val monitor = NetworkMonitor.getInstance(context, prefs)
            val constraints =
                monitor.buildConstraints(
                    allowMetered = prefs.downloadAllowMeteredFlow.first(),
                    requireBatteryNotLow = !prefs.downloadAllowLowBatteryFlow.first(),
                )
            val request =
                OneTimeWorkRequestBuilder<DownloadWorkerV2>()
                    .setInputData(workDataOf(KavitaProfile.WORK_INPUT to profile.id))
                    .setConstraints(constraints)
                    .build()
            // A worker can be exiting after its last queue read, so a new request must get its own turn.
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork("$WORK_NAME-${profile.id}", ExistingWorkPolicy.APPEND_OR_REPLACE, request)
        }
    }
}
