package com.iquintero.kavern.data.api.dto

data class BookChapterItemDto(
    val title: String?,
    val part: String?,
    val page: Int?,
    val children: List<BookChapterItemDto>?,
)
