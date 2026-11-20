package com.iquintero.kavern.ui.seriesdetail

import com.iquintero.kavern.core.logging.LoggingManager
import com.iquintero.kavern.core.storage.KavitaProfile
import com.iquintero.kavern.data.api.dto.VolumeDto

data class VolumeDetailPayload(
    val seriesId: Int,
    val libraryId: Int? = null,
    val volume: VolumeDto,
    val formatId: Int? = null,
)

object VolumeDetailCache {
    private val data = mutableMapOf<Pair<KavitaProfile, Int>, VolumeDetailPayload>()

    fun put(
        profile: KavitaProfile,
        payload: VolumeDetailPayload,
    ) {
        data[profile to payload.volume.id] = payload
        if (LoggingManager.isDebugEnabled()) {
            LoggingManager.d(
                "VolumeDetailCache",
                "Put volume=${payload.volume.id} series=${payload.seriesId}",
            )
        }
    }

    fun get(
        profile: KavitaProfile,
        volumeId: Int,
    ): VolumeDetailPayload? {
        val payload = data[profile to volumeId]
        if (LoggingManager.isDebugEnabled()) {
            LoggingManager.d(
                "VolumeDetailCache",
                "Get volume=$volumeId hit=${payload != null}",
            )
        }
        return payload
    }
}
