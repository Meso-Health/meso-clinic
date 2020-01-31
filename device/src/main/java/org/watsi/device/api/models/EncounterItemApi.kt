package org.watsi.device.api.models

import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.LabResult
import java.util.UUID

data class EncounterItemApi(
    val id: UUID,
    val encounterId: UUID,
    val quantity: Int,
    val priceScheduleId: UUID,
    val priceScheduleIssued: Boolean,
    val stockout: Boolean,
    val surgicalScore: Int?,
    val labResult: LabResultApi?
) {

    fun toEncounterItem(): EncounterItem {
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

    constructor (encounterItem: EncounterItem, labResult: LabResult?) : this(
        id = encounterItem.id,
        encounterId = encounterItem.encounterId,
        quantity = encounterItem.quantity,
        priceScheduleId = encounterItem.priceScheduleId,
        priceScheduleIssued = encounterItem.priceScheduleIssued,
        stockout = encounterItem.stockout,
        surgicalScore = encounterItem.surgicalScore,
        labResult = labResult?.let { LabResultApi(labResult) }
    )
}
