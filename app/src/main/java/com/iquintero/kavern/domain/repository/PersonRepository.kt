package com.iquintero.kavern.domain.repository

import com.iquintero.kavern.domain.model.Person

interface PersonRepository {
    suspend fun getBrowsePeople(
        pageNumber: Int = 1,
        pageSize: Int = 50,
    ): List<Person>
}
