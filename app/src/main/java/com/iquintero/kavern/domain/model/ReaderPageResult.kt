package com.iquintero.kavern.domain.model

data class ReaderPageResult(
    val html: String,
    val fromOffline: Boolean,
    val offlineAssetPaths: Set<String> = emptySet(),
)
