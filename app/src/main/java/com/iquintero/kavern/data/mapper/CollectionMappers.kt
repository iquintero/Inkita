package com.iquintero.kavern.data.mapper

import com.iquintero.kavern.data.api.dto.AppUserCollectionDto
import com.iquintero.kavern.data.api.dto.BrowsePersonDto
import com.iquintero.kavern.data.api.dto.CollectionDto
import com.iquintero.kavern.data.api.dto.ReadingListDto
import com.iquintero.kavern.domain.model.Collection
import com.iquintero.kavern.domain.model.Person
import com.iquintero.kavern.domain.model.ReadingList

fun CollectionDto.toDomain(): Collection =
    Collection(
        id = id,
        name = title,
    )

fun AppUserCollectionDto.toDomain(): Collection =
    Collection(
        id = id,
        name = title.orEmpty(),
    )

fun ReadingListDto.toDomain(): ReadingList =
    ReadingList(
        id = id,
        title = title.orEmpty(),
        itemCount = itemCount,
    )

fun BrowsePersonDto.toDomain(): Person =
    Person(
        id = id,
        name = name,
    )
