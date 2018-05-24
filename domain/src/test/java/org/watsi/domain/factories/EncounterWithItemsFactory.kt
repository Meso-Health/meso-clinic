package org.watsi.domain.factories

import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.EncounterWithItems

object EncounterWithItemsFactory {
    fun build(encounter: Encounter = EncounterFactory.build(),
              items: List<EncounterItem> = emptyList()
    ) : EncounterWithItems {
        return EncounterWithItems(encounter, items)
    }
}
