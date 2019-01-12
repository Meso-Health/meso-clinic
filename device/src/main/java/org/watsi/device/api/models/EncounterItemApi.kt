package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.EncounterItem
import java.util.UUID

data class EncounterItemApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("encounter_id") val encounterId: UUID,
        @SerializedName("billable_id") val billableId: UUID,
        @SerializedName("quantity") val quantity: Int,
        @SerializedName("price_schedule_id") val priceScheduleId: UUID,
        @SerializedName("price_schedule_issued") val priceScheduleIssued: Boolean
) {

    fun toEncounterItem(): EncounterItem {
        return EncounterItem(
            id = id,
            encounterId = encounterId,
            billableId = billableId,
            quantity = quantity,
            priceScheduleId = priceScheduleId,
            priceScheduleIssued = priceScheduleIssued
        )
    }

    constructor (encounterItem: EncounterItem) : this(
        id = encounterItem.id,
        encounterId = encounterItem.encounterId,
        billableId = encounterItem.billableId,
        quantity = encounterItem.quantity,
        priceScheduleId = encounterItem.priceScheduleId,
        priceScheduleIssued = encounterItem.priceScheduleIssued
    )
}
