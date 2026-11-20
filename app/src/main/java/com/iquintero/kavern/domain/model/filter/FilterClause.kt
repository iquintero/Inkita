package com.iquintero.kavern.domain.model.filter

data class FilterClause(
    val field: KavitaField,
    val comparison: KavitaComparison,
    val value: String,
)
