package com.iquintero.kavern.data.api.dto

data class RatingDto(
    val averageScore: Int? = null,
    val favoriteCount: Int? = null,
    val provider: Int? = null,
    val authority: Int? = null,
    val providerUrl: String? = null,
)
