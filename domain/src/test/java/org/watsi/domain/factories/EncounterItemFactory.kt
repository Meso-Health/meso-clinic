package org.watsi.domain.factories

import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.PriceSchedule
import java.util.UUID

object EncounterItemFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        encounterId: UUID = UUID.randomUUID(),
        billableId: UUID = UUID.randomUUID(),
        quantity: Int = 1,
        priceScheduleId: UUID = UUID.randomUUID(),
        priceScheduleIssued: Boolean = false
    ): EncounterItem {
        return EncounterItem(
            id = id,
            encounterId = encounterId,
            billableId = billableId,
            quantity = quantity,
            priceScheduleId = priceScheduleId,
            priceScheduleIssued = priceScheduleIssued
        )
    }
}
