package com.iquintero.kavern.domain.model

data class ReadingList(
    val id: Int,
    val title: String,
    val itemCount: Int? = null,
)
