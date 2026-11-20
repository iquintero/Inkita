// domain/repository/CollectionsRepository.kt
package com.iquintero.kavern.domain.repository

import com.iquintero.kavern.domain.model.Collection
import com.iquintero.kavern.domain.model.Series

interface CollectionsRepository {
    suspend fun getCollections(): List<Collection>

    suspend fun getCollectionsAll(ownedOnly: Boolean = false): List<Collection>

    suspend fun getSeriesForCollection(
        collectionId: Int,
        page: Int = 1,
        pageSize: Int = 50,
    ): List<Series>
}
