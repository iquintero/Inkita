package com.iquintero.kavern.data.api.dto

data class CollectionTagBulkAddDto(
    val collectionTagId: Int,
    val collectionTagTitle: String? = null,
    val seriesIds: List<Int> = emptyList(),
)
