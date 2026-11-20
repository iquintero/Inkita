package com.iquintero.kavern.data.repository

import android.content.Context
import com.iquintero.kavern.core.network.KavitaApiFactory
import com.iquintero.kavern.core.network.NetworkUtils
import com.iquintero.kavern.core.storage.AppPreferences
import com.iquintero.kavern.data.api.dto.BrowsePersonFilterDto
import com.iquintero.kavern.data.api.dto.PersonFilterStatementDto
import com.iquintero.kavern.data.api.dto.PersonSortOptionsDto
import com.iquintero.kavern.data.mapper.toDomain
import com.iquintero.kavern.domain.model.Person
import com.iquintero.kavern.domain.repository.PersonRepository
import kotlinx.coroutines.flow.first
import java.io.IOException

class PersonRepositoryImpl(
    private val context: Context,
    private val appPreferences: AppPreferences,
) : PersonRepository {
    override suspend fun getBrowsePeople(
        pageNumber: Int,
        pageSize: Int,
    ): List<Person> {
        val config = appPreferences.configFlow.first()
        if (!config.isConfigured) return emptyList()

        if (!NetworkUtils.isOnline(context)) throw IOException("Offline")

        val api = KavitaApiFactory.createAuthenticated(config)
        val filter =
            BrowsePersonFilterDto(
                id = null,
                name = null,
                statements =
                    listOf(
                        PersonFilterStatementDto(
                            comparison = 5,
                            field = 1,
                            value = "8,3",
                        ),
                    ),
                combination = 1,
                sortOptions =
                    PersonSortOptionsDto(
                        isAscending = true,
                        sortField = 1,
                    ),
                limitTo = 0,
            )

        val response =
            api.getBrowsePeople(
                filter = filter,
                pageNumber = pageNumber,
                pageSize = pageSize,
            )

        if (!response.isSuccessful) {
            val body = response.errorBody()?.string()
            throw Exception("Error loading people: HTTP ${response.code()} ${response.message()} body=$body")
        }

        val dtos = response.body() ?: emptyList()
        return dtos.map { it.toDomain() }
    }
}
