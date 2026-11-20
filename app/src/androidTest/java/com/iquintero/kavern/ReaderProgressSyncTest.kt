package com.iquintero.kavern

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iquintero.kavern.data.local.db.KavernDatabase
import com.iquintero.kavern.data.local.db.dao.ReaderDao
import com.iquintero.kavern.data.local.db.entity.LocalReaderProgressEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderProgressSyncTest {
    @Test
    fun staleSyncLeavesNewerLocalProgressIntact() =
        withReaderDao { dao ->
            val initial = LocalReaderProgressEntity(chapterId = 1, page = 10, lastModifiedUtc = 1000L)
            val newer = initial.copy(page = 11)
            dao.upsertLocalProgress(initial)
            val snapshot = dao.getAllLocalProgress().single()
            dao.upsertLocalProgress(newer)

            dao.finishProgressSync(snapshot, replacement = null)
            assertEquals(newer, dao.getLocalProgress(initial.chapterId))

            dao.finishProgressSync(snapshot, replacement = initial.copy(page = 9))
            assertEquals(newer, dao.getLocalProgress(initial.chapterId))
        }

    @Test
    fun syncCanUpdateAndClearUnchangedLocalProgress() =
        withReaderDao { dao ->
            val initial = LocalReaderProgressEntity(chapterId = 1, page = 10)
            val replacement = initial.copy(page = 11)
            dao.upsertLocalProgress(initial)

            dao.finishProgressSync(initial, replacement)
            assertEquals(replacement, dao.getLocalProgress(initial.chapterId))

            dao.finishProgressSync(replacement, replacement = null)
            assertNull(dao.getLocalProgress(initial.chapterId))

            dao.finishProgressSync(replacement, replacement = initial)
            assertNull(dao.getLocalProgress(initial.chapterId))
        }

    private fun withReaderDao(block: suspend (ReaderDao) -> Unit) =
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val database = Room.inMemoryDatabaseBuilder(context, KavernDatabase::class.java).build()
            try {
                block(database.readerDao())
            } finally {
                database.close()
            }
        }
}
