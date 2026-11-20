package com.iquintero.kavern.ui.reader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iquintero.kavern.domain.reader.PdfReader
import com.iquintero.kavern.domain.repository.ReaderRepository
import kotlinx.coroutines.flow.update

class PdfReaderViewModel(
    chapterId: Int,
    initialPage: Int,
    readerRepository: ReaderRepository,
    seriesId: Int?,
    volumeId: Int?,
    anonymous: Boolean = false,
) : BaseReaderViewModel(
        chapterId = chapterId,
        initialPage = initialPage,
        reader = PdfReader(readerRepository),
        seriesId = seriesId,
        volumeId = volumeId,
        anonymous = anonymous,
    ) {
    override fun loadPage(index: Int) {
        _state.update { it.copy(pageIndex = index) }
        updateProgress(index, totalPagesOverride = _state.value.pageCount)
    }

    fun setPdfPageIndex(
        index: Int,
        pageCount: Int,
    ) {
        _state.update { it.copy(pageCount = pageCount, pageIndex = index) }
        updateProgress(index, totalPagesOverride = pageCount)
    }

    override suspend fun isPageDownloaded(pageIndex: Int): Boolean = true

    companion object {
        fun provideFactory(
            chapterId: Int,
            initialPage: Int,
            readerRepository: ReaderRepository,
            seriesId: Int?,
            volumeId: Int?,
            anonymous: Boolean = false,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return PdfReaderViewModel(
                        chapterId,
                        initialPage,
                        readerRepository,
                        seriesId,
                        volumeId,
                        anonymous,
                    ) as T
                }
            }
    }
}
