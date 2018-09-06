package org.watsi.domain.factories

import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice

object EncounterItemWithBillableAndPriceFactory {
    fun build(
        priceScheduleAndBillable: BillableWithPriceSchedule = BillableWithPriceScheduleFactory.build(),
        encounterItem: EncounterItem = EncounterItemFactory.build(
            billableId = priceScheduleAndBillable.billable.id,
            priceScheduleId = priceScheduleAndBillable.priceSchedule.id
        )
    ): EncounterItemWithBillableAndPrice {
        return EncounterItemWithBillableAndPrice(encounterItem, priceScheduleAndBillable)
    }
}
