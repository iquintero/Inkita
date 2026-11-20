package com.iquintero.kavern.data.mapper

import com.iquintero.kavern.data.api.dto.LibraryDto
import com.iquintero.kavern.domain.model.Library

fun LibraryDto.toDomain(): Library =
    Library(
        id = id,
        name = name.orEmpty(),
    )
