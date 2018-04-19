package org.watsi.domain.entities

import java.util.UUID

data class EncounterForm(val id: UUID,
                         val encounterId: UUID,
                         val photoId: UUID)
