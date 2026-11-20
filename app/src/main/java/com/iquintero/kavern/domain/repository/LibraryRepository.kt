package com.iquintero.kavern.domain.repository

import com.iquintero.kavern.domain.model.Library

interface LibraryRepository {
    suspend fun getLibraries(): List<Library>

    suspend fun hasLibraryAccess(libraryId: Int): Boolean
}
