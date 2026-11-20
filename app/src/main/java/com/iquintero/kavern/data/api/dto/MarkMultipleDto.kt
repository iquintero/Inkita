package com.iquintero.kavern.data.api.dto

data class MarkMultipleDto(
    val seriesId: Int,
    val volumeIds: List<Int> = emptyList(),
    val chapterIds: List<Int> = emptyList(),
)
