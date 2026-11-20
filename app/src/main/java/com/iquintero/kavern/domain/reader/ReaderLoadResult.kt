package com.iquintero.kavern.domain.reader

data class ReaderLoadResult(
    val content: String? = null,
    val fromOffline: Boolean = false,
    val pdfPath: String? = null,
    val imageUrl: String? = null,
    val offlineAssetPaths: Set<String> = emptySet(),
)
