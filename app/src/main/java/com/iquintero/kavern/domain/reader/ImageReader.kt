package com.iquintero.kavern.domain.reader

import com.iquintero.kavern.domain.model.Format
import com.iquintero.kavern.domain.model.ReaderBookInfo
import com.iquintero.kavern.domain.model.ReaderChapterNav
import com.iquintero.kavern.domain.model.ReaderProgress
import com.iquintero.kavern.domain.model.ReaderTimeLeft
import com.iquintero.kavern.domain.repository.ReaderRepository

class ImageReader(
    private val readerRepository: ReaderRepository,
) : BaseReader {
    override val format: Format = Format.Image

    override suspend fun loadInitial(
        chapterId: Int,
        initialPage: Int,
    ): ReaderLoadResult = loadPage(chapterId, initialPage)

    override suspend fun loadPage(
        chapterId: Int,
        pageIndex: Int,
    ): ReaderLoadResult {
        val result = readerRepository.getImageResult(chapterId, pageIndex)
        return ReaderLoadResult(
            imageUrl = result.url,
            fromOffline = result.fromOffline,
        )
    }

    override suspend fun getBookInfo(chapterId: Int): ReaderBookInfo? = readerRepository.getBookInfo(chapterId)

    override suspend fun getProgress(chapterId: Int): ReaderProgress? = readerRepository.getProgress(chapterId)

    override suspend fun setProgress(
        progress: ReaderProgress,
        totalPages: Int?,
    ) {
        readerRepository.setProgress(progress, totalPages)
    }

    override suspend fun saveProgressLocally(progress: ReaderProgress) {
        readerRepository.saveProgressLocally(progress)
    }

    override suspend fun getTimeLeft(
        seriesId: Int,
        chapterId: Int,
    ): ReaderTimeLeft? = readerRepository.getTimeLeft(seriesId, chapterId)

    override suspend fun isPageDownloaded(
        chapterId: Int,
        pageIndex: Int,
    ): Boolean = readerRepository.isPageDownloaded(chapterId, pageIndex)

    override suspend fun getNextChapter(
        seriesId: Int,
        volumeId: Int,
        currentChapterId: Int,
    ): ReaderChapterNav? = readerRepository.getNextChapter(seriesId, volumeId, currentChapterId)

    override suspend fun getPreviousChapter(
        seriesId: Int,
        volumeId: Int,
        currentChapterId: Int,
    ): ReaderChapterNav? = readerRepository.getPreviousChapter(seriesId, volumeId, currentChapterId)
}
