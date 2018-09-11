package org.watsi.domain.factories

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.relations.EncounterWithItems

object EncounterWithItemsFactory {
    fun build(
            encounter: Encounter = EncounterFactory.build(),
            items: List<EncounterItem> = emptyList()
    ) : EncounterWithItems {
        return EncounterWithItems(encounter, items)
    }

    fun buildWithBillableAndPriceSchedule(
            encounter: Encounter = EncounterFactory.build(),
            billable: Billable,
            priceSchedule: PriceSchedule
    ) : EncounterWithItems {
        val encounterItem = EncounterItemFactory.build(encounterId = encounter.id, billableId = billable.id, priceScheduleId = priceSchedule.id)
        return EncounterWithItems(encounter, listOf(encounterItem))
    }
}
