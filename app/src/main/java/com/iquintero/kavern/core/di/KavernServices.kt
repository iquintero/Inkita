package com.iquintero.kavern.core.di

import android.content.Context
import com.iquintero.kavern.core.storage.AppPreferences
import com.iquintero.kavern.data.local.db.KavernDatabase
import com.iquintero.kavern.data.repository.ReaderRepositoryImpl
import com.iquintero.kavern.domain.repository.ReaderRepository

/**
 * Very small service locator to provide dependencies for background workers
 * (e.g., ProgressSyncWorker) where we can't easily pass objects directly.
 */
data class KavernServices(
    val readerRepository: ReaderRepository,
) {
    companion object {
        fun get(
            context: Context,
            appPreferences: AppPreferences,
        ): KavernServices {
            val database = KavernDatabase.getInstance(context, appPreferences.profile)
            val readerRepository =
                ReaderRepositoryImpl(
                    context,
                    appPreferences,
                    database.readerDao(),
                    database.downloadV2Dao(),
                )
            return KavernServices(readerRepository = readerRepository)
        }
    }
}
