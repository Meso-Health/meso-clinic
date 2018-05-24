package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.EncounterItem
import java.util.UUID

data class EncounterItemApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("encounter_id") val encounterId: UUID,
        @SerializedName("billable_id") val billableId: UUID,
        @SerializedName("quantity") val quantity: Int
) {

    constructor (encounterItem: EncounterItem) :
            this(id = encounterItem.id,
                 encounterId = encounterItem.encounterId,
                 billableId = encounterItem.billableId,
                 quantity = encounterItem.quantity
            )
}
