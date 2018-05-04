package org.watsi.domain.factories

import org.watsi.domain.entities.EncounterItem
import java.util.UUID

object EncounterItemFactory {

    fun build(id: UUID = UUID.randomUUID(),
              encounterId: UUID = UUID.randomUUID(),
              billableId: UUID = UUID.randomUUID(),
              quantity: Int = 1) : EncounterItem {
        return EncounterItem(id = id,
                             encounterId = encounterId,
                             billableId = billableId,
                             quantity = quantity)
    }
}
