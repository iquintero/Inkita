// data/repository/CollectionsRepositoryImpl.kt
package com.iquintero.kavern.data.repository

import android.content.Context
import com.iquintero.kavern.core.network.KavitaApiFactory
import com.iquintero.kavern.core.network.NetworkUtils
import com.iquintero.kavern.core.storage.AppPreferences
import com.iquintero.kavern.data.mapper.toDomain
import com.iquintero.kavern.domain.model.Collection
import com.iquintero.kavern.domain.model.Series
import com.iquintero.kavern.domain.repository.CollectionsRepository
import kotlinx.coroutines.flow.first
import java.io.IOException

class CollectionsRepositoryImpl(
    private val context: Context,
    private val appPreferences: AppPreferences,
) : CollectionsRepository {
    override suspend fun getCollections(): List<Collection> {
        val config = appPreferences.configFlow.first()
        if (!config.isConfigured) return emptyList()

        if (!NetworkUtils.isOnline(context)) throw IOException("Offline")

        val api = KavitaApiFactory.createAuthenticated(config)
        val response = api.getCollections()

        if (!response.isSuccessful) {
            val body = response.errorBody()?.string()
            throw Exception("Error loading collections: HTTP ${response.code()} ${response.message()} body=$body")
        }

        val dtos = response.body() ?: emptyList()
        return dtos.map { it.toDomain() }
    }

    override suspend fun getCollectionsAll(ownedOnly: Boolean): List<Collection> {
        val config = appPreferences.configFlow.first()
        if (!config.isConfigured) return emptyList()

        if (!NetworkUtils.isOnline(context)) throw IOException("Offline")

        val api = KavitaApiFactory.createAuthenticated(config)
        val response = api.getCollectionsAll(ownedOnly = ownedOnly)

        if (!response.isSuccessful) {
            val body = response.errorBody()?.string()
            throw Exception("Error loading collections: HTTP ${response.code()} ${response.message()} body=$body")
        }

        val dtos = response.body() ?: emptyList()
        return dtos.map { it.toDomain() }
    }

    override suspend fun getSeriesForCollection(
        collectionId: Int,
        page: Int,
        pageSize: Int,
    ): List<Series> {
        val config = appPreferences.configFlow.first()
        if (!config.isConfigured) return emptyList()

        if (!NetworkUtils.isOnline(context)) throw IOException("Offline")

        val api = KavitaApiFactory.createAuthenticated(config)
        val response =
            api.getSeriesByCollection(
                collectionId = collectionId,
                pageNumber = page,
                pageSize = pageSize,
            )

        if (!response.isSuccessful) {
            val body = response.errorBody()?.string()
            throw Exception("Error loading collection series: HTTP ${response.code()} ${response.message()} body=$body")
        }

        val body = response.body() ?: emptyList()
        return body
            .map { it.toDomain() }
            .filter { series ->
                series.format == com.iquintero.kavern.domain.model.Format.Epub || series.format == com.iquintero.kavern.domain.model.Format.Pdf
            }
    }
}
