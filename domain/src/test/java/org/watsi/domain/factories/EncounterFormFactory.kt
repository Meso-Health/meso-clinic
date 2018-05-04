package org.watsi.domain.factories

import org.watsi.domain.entities.EncounterForm
import java.util.UUID

object EncounterFormFactory {

    fun build(id: UUID = UUID.randomUUID(),
              encounterId: UUID = UUID.randomUUID(),
              photoId: UUID = UUID.randomUUID()) : EncounterForm {
        return EncounterForm(id = id, encounterId = encounterId, photoId = photoId)
    }
}
