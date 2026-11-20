package com.iquintero.kavern.data.mapper

import com.iquintero.kavern.data.api.dto.RecentlyAddedItemDto
import com.iquintero.kavern.domain.model.RecentlyUpdatedSeriesItem

fun RecentlyAddedItemDto.toDomain(): RecentlyUpdatedSeriesItem =
    RecentlyUpdatedSeriesItem(
        seriesName = seriesName,
        seriesId = seriesId,
        libraryId = libraryId,
        libraryType = libraryType,
        title = title,
        created = created,
        chapterId = chapterId,
        volumeId = volumeId,
        id = id,
        format = format,
    )
