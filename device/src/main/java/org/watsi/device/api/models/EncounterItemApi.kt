package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.relations.EncounterItemWithBillable
import java.util.UUID

data class EncounterItemApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("encounter_id") val encounterId: UUID,
        @SerializedName("billable_id") val billableId: UUID,
        @SerializedName("quantity") val quantity: Int
) {

    constructor (encounterItemWithBillable: EncounterItemWithBillable) :
            this(id = encounterItemWithBillable.encounterItem.id,
                 encounterId = encounterItemWithBillable.encounterItem.encounterId,
                 billableId = encounterItemWithBillable.encounterItem.billableId,
                 quantity = encounterItemWithBillable.encounterItem.quantity
            )
}
