package com.iquintero.kavern.domain.repository

import com.iquintero.kavern.domain.model.RecentlyUpdatedSeriesItem
import com.iquintero.kavern.domain.model.Series
import com.iquintero.kavern.domain.model.filter.KavitaSortField
import com.iquintero.kavern.domain.model.filter.SeriesQuery

interface SeriesRepository {
    suspend fun getSeries(
        query: SeriesQuery,
        prefetchThumbnails: Boolean = true,
    ): List<Series>

    suspend fun getOnDeckSeries(
        pageNumber: Int,
        pageSize: Int,
        libraryId: Int = 0,
    ): List<Series>

    suspend fun getRecentlyUpdatedSeries(
        pageNumber: Int,
        pageSize: Int,
    ): List<RecentlyUpdatedSeriesItem>

    suspend fun getRecentlyAddedSeries(
        pageNumber: Int,
        pageSize: Int,
    ): List<Series>

    suspend fun getWantToReadSeries(
        pageNumber: Int,
        pageSize: Int,
    ): List<Series>

    suspend fun getSeriesForCollection(
        collectionId: Int,
        pageNumber: Int,
        pageSize: Int,
    ): List<Series>

    /** Every series the user can see, across all libraries, in the given order. */
    suspend fun getAllSeries(
        sortField: KavitaSortField,
        ascending: Boolean,
        pageNumber: Int,
        pageSize: Int,
    ): List<Series>

    suspend fun getSeriesForLibrary(
        libraryId: Int,
        pageNumber: Int,
        pageSize: Int,
    ): List<Series>
}
