package org.watsi.domain.factories

import org.watsi.domain.entities.EncounterItem
import java.util.UUID

object EncounterItemFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        encounterId: UUID = UUID.randomUUID(),
        quantity: Int = 1,
        priceScheduleId: UUID = UUID.randomUUID(),
        priceScheduleIssued: Boolean = false,
        stockout: Boolean = false,
        surgicalScore: Int? = null
    ): EncounterItem {
        return EncounterItem(
            id = id,
            encounterId = encounterId,
            quantity = quantity,
            priceScheduleId = priceScheduleId,
            priceScheduleIssued = priceScheduleIssued,
            stockout = stockout,
            surgicalScore = surgicalScore
        )
    }
}
