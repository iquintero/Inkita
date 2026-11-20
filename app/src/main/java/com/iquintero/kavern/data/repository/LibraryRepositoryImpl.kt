package com.iquintero.kavern.data.repository

import android.content.Context
import com.iquintero.kavern.core.network.KavitaApiFactory
import com.iquintero.kavern.core.network.NetworkUtils
import com.iquintero.kavern.core.storage.AppPreferences
import com.iquintero.kavern.data.mapper.toDomain
import com.iquintero.kavern.domain.model.Library
import com.iquintero.kavern.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.first
import java.io.IOException

class LibraryRepositoryImpl(
    private val context: Context,
    private val appPreferences: AppPreferences,
) : LibraryRepository {
    override suspend fun getLibraries(): List<Library> {
        val config = appPreferences.configFlow.first()

        if (!config.isConfigured) {
            return emptyList()
        }

        if (!NetworkUtils.isOnline(context)) {
            throw IOException("Offline")
        }

        val api =
            KavitaApiFactory.createAuthenticated(config)

        val response = api.getLibrariesFilter()

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code()} ${response.message()}")
        }

        val dtos = response.body() ?: emptyList()
        return dtos.map { it.toDomain() }
    }

    override suspend fun hasLibraryAccess(libraryId: Int): Boolean {
        val config = appPreferences.configFlow.first()

        if (!config.isConfigured) {
            return false
        }

        if (!NetworkUtils.isOnline(context)) {
            throw IOException("Offline")
        }

        val api =
            KavitaApiFactory.createAuthenticated(config)

        val response = api.hasLibraryAccess(libraryId)

        if (!response.isSuccessful) {
            throw Exception("Error checking library access: HTTP ${response.code()} ${response.message()}")
        }

        return response.body() ?: false
    }
}
