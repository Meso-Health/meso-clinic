package org.watsi.device.api.models

import org.watsi.domain.entities.EncounterItem
import java.util.UUID

data class EncounterItemApi(
    val id: UUID,
    val encounterId: UUID,
    val quantity: Int,
    val priceScheduleId: UUID,
    val priceScheduleIssued: Boolean,
    val stockout: Boolean
) {

    fun toEncounterItem(): EncounterItem {
        return EncounterItem(
            id = id,
            encounterId = encounterId,
            quantity = quantity,
            priceScheduleId = priceScheduleId,
            priceScheduleIssued = priceScheduleIssued,
            stockout = stockout
        )
    }

    constructor (encounterItem: EncounterItem) : this(
        id = encounterItem.id,
        encounterId = encounterItem.encounterId,
        quantity = encounterItem.quantity,
        priceScheduleId = encounterItem.priceScheduleId,
        priceScheduleIssued = encounterItem.priceScheduleIssued,
        stockout = encounterItem.stockout
    )
}
