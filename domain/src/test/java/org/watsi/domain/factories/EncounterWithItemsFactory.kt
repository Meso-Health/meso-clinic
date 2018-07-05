package org.watsi.domain.factories

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.EncounterWithItems

object EncounterWithItemsFactory {
    fun build(
            encounter: Encounter = EncounterFactory.build(),
            items: List<EncounterItem> = emptyList()
    ) : EncounterWithItems {
        return EncounterWithItems(encounter, items)
    }

    fun buildWithBillable(
            encounter: Encounter = EncounterFactory.build(),
            billable: Billable
    ) : EncounterWithItems {
        val encounterItem = EncounterItemFactory.build(encounterId = encounter.id, billableId = billable.id)
        return EncounterWithItems(encounter, listOf(encounterItem))
    }
}
