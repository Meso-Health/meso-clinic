package org.watsi.domain.entities

import java.io.Serializable
import java.util.UUID

data class EncounterForm(val id: UUID,
                         val encounterId: UUID,
                         val photoId: UUID?,
                         val thumbnailId: UUID?) : Serializable

