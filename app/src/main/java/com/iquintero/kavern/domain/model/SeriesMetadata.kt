package com.iquintero.kavern.domain.model

data class SeriesMetadata(
    val summary: String?,
    val tags: List<Tag>,
    val writers: List<Person>,
    val publicationStatus: Int?,
)
