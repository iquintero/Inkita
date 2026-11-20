package com.iquintero.kavern.ui.seriesdetail

import com.iquintero.kavern.data.api.dto.AnnotationDto
import com.iquintero.kavern.data.api.dto.AppUserCollectionDto
import com.iquintero.kavern.data.api.dto.BookmarkDto
import com.iquintero.kavern.data.api.dto.ChapterDto
import com.iquintero.kavern.data.api.dto.HourEstimateRangeDto
import com.iquintero.kavern.data.api.dto.RatingDto
import com.iquintero.kavern.data.api.dto.ReaderProgressDto
import com.iquintero.kavern.data.api.dto.ReadingListDto
import com.iquintero.kavern.data.api.dto.RelatedSeriesDto
import com.iquintero.kavern.data.api.dto.SeriesDetailDto
import com.iquintero.kavern.data.api.dto.SeriesDetailPlusDto
import com.iquintero.kavern.data.api.dto.SeriesDto
import com.iquintero.kavern.data.api.dto.SeriesMetadataDto

data class SeriesDetailUiStateV2(
    val isLoading: Boolean = true,
    val error: String? = null,
    val detail: KavernDetailV2? = null,
    val showLoadedToast: Boolean = false,
    val collections: List<com.iquintero.kavern.domain.model.Collection> = emptyList(),
    val isLoadingCollections: Boolean = false,
    val collectionError: String? = null,
    val collectionsWithSeries: Set<Int> = emptySet(),
)

data class KavernDetailV2(
    val series: SeriesDto?,
    val metadata: SeriesMetadataDto?,
    val wantToRead: Boolean?,
    val readingLists: List<ReadingListDto>?,
    val collections: List<AppUserCollectionDto>?,
    val bookmarks: List<BookmarkDto>?,
    val annotations: List<AnnotationDto>?,
    val timeLeft: HourEstimateRangeDto?,
    val hasProgress: Boolean?,
    val continuePoint: ChapterDto?,
    val seriesDetailPlus: SeriesDetailPlusDto?,
    val related: RelatedSeriesDto?,
    val detail: SeriesDetailDto?,
    val rating: RatingDto?,
    val readerProgress: ReaderProgressDto?,
)
