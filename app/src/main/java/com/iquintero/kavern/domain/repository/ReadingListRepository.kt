package com.iquintero.kavern.domain.repository

import com.iquintero.kavern.domain.model.ReadingList

interface ReadingListRepository {
    suspend fun getReadingLists(
        includePromoted: Boolean = true,
        sortByLastModified: Boolean = false,
        pageNumber: Int = 1,
        pageSize: Int = 50,
    ): List<ReadingList>
}
