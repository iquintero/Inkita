package com.iquintero.kavern.data.repository

import android.content.Context
import com.iquintero.kavern.core.network.KavitaApiFactory
import com.iquintero.kavern.core.network.NetworkUtils
import com.iquintero.kavern.core.storage.AppPreferences
import com.iquintero.kavern.data.mapper.toDomain
import com.iquintero.kavern.domain.model.ReadingList
import com.iquintero.kavern.domain.repository.ReadingListRepository
import kotlinx.coroutines.flow.first
import java.io.IOException

class ReadingListRepositoryImpl(
    private val context: Context,
    private val appPreferences: AppPreferences,
) : ReadingListRepository {
    override suspend fun getReadingLists(
        includePromoted: Boolean,
        sortByLastModified: Boolean,
        pageNumber: Int,
        pageSize: Int,
    ): List<ReadingList> {
        val config = appPreferences.configFlow.first()
        if (!config.isConfigured) return emptyList()

        if (!NetworkUtils.isOnline(context)) throw IOException("Offline")

        val api = KavitaApiFactory.createAuthenticated(config)
        val response =
            api.getReadingLists(
                includePromoted = includePromoted,
                sortByLastModified = sortByLastModified,
                pageNumber = pageNumber,
                pageSize = pageSize,
            )

        if (!response.isSuccessful) {
            val body = response.errorBody()?.string()
            throw Exception("Error loading reading lists: HTTP ${response.code()} ${response.message()} body=$body")
        }

        val dtos = response.body() ?: emptyList()
        return dtos.map { it.toDomain() }
    }
}
