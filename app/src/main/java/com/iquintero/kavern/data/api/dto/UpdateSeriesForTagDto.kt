package com.iquintero.kavern.data.api.dto

data class UpdateSeriesForTagDto(
    val tag: AppUserCollectionDto,
    val seriesIdsToRemove: List<Int> = emptyList(),
)
