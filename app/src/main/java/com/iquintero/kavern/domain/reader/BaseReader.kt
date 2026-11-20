package com.iquintero.kavern.domain.reader

import com.iquintero.kavern.domain.model.Format
import com.iquintero.kavern.domain.model.ReaderBookInfo
import com.iquintero.kavern.domain.model.ReaderChapterNav
import com.iquintero.kavern.domain.model.ReaderProgress
import com.iquintero.kavern.domain.model.ReaderTimeLeft
import com.iquintero.kavern.domain.model.ReaderTocItem

interface BaseReader {
    val format: Format

    suspend fun loadInitial(
        chapterId: Int,
        initialPage: Int,
    ): ReaderLoadResult

    suspend fun loadPage(
        chapterId: Int,
        pageIndex: Int,
    ): ReaderLoadResult

    suspend fun getBookInfo(chapterId: Int): ReaderBookInfo?

    suspend fun getToc(chapterId: Int): List<ReaderTocItem> = emptyList()

    suspend fun getLocalProgress(chapterId: Int): ReaderProgress? = null

    suspend fun getCachedBookInfo(chapterId: Int): ReaderBookInfo? = null

    suspend fun getProgress(chapterId: Int): ReaderProgress?

    suspend fun setProgress(
        progress: ReaderProgress,
        totalPages: Int? = null,
    )

    suspend fun saveProgressLocally(progress: ReaderProgress)

    suspend fun getTimeLeft(
        seriesId: Int,
        chapterId: Int,
    ): ReaderTimeLeft?

    suspend fun isPageDownloaded(
        chapterId: Int,
        pageIndex: Int,
    ): Boolean

    suspend fun getNextChapter(
        seriesId: Int,
        volumeId: Int,
        currentChapterId: Int,
    ): ReaderChapterNav?

    suspend fun getPreviousChapter(
        seriesId: Int,
        volumeId: Int,
        currentChapterId: Int,
    ): ReaderChapterNav?
}
