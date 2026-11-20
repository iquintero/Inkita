package com.iquintero.kavern.data.api.dto

data class ReaderProgressDto(
    val volumeId: Int? = null,
    val chapterId: Int,
    val pageNum: Int? = null,
    val seriesId: Int? = null,
    val libraryId: Int? = null,
    val bookScrollId: String? = null,
    val lastModifiedUtc: String? = null,
)
