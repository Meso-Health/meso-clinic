package org.watsi.domain.factories

import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.LabResult
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import java.util.UUID

object EncounterItemWithBillableAndPriceFactory {
    fun build(
        billableWithPrice: BillableWithPriceSchedule = BillableWithPriceScheduleFactory.build(),
        encounterItem: EncounterItem = EncounterItemFactory.build(
            priceScheduleId = billableWithPrice.priceSchedule.id
        ),
        labResult: LabResult? = null
    ): EncounterItemWithBillableAndPrice {
        return EncounterItemWithBillableAndPrice(
            encounterItem = encounterItem,
            billableWithPriceSchedule = billableWithPrice,
            labResult = labResult
        )
    }

    fun buildWithEncounter(
        encounterId: UUID,
        billableWithPrice: BillableWithPriceSchedule = BillableWithPriceScheduleFactory.build(),
        labResult: LabResult? = null,
        encounterItem: EncounterItem = EncounterItemFactory.build(
            encounterId = encounterId,
            priceScheduleId = billableWithPrice.priceSchedule.id
        )
    ): EncounterItemWithBillableAndPrice {
        return EncounterItemWithBillableAndPrice(
            encounterItem = encounterItem,
            billableWithPriceSchedule = billableWithPrice,
            labResult = labResult
        )
    }
}
