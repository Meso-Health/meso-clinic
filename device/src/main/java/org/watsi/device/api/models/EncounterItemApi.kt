package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterItemWithBillable
import java.util.UUID

data class EncounterItemApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("encounter_id") val encounterId: UUID,
        @SerializedName("billable") val billable: BillableApi,
        @SerializedName("quantity") val quantity: Int
) {

    constructor (encounterItemWithBillable: EncounterItemWithBillable) :
            this(id = encounterItemWithBillable.encounterItem.id,
                 encounterId = encounterItemWithBillable.encounterItem.encounterId,
                 billable = BillableApi(
                         encounterItemWithBillable.billable.id,
                         encounterItemWithBillable.billable.type,
                         encounterItemWithBillable.billable.composition,
                         encounterItemWithBillable.billable.unit,
                         encounterItemWithBillable.billable.price,
                         encounterItemWithBillable.billable.name
                 ),
                 quantity = encounterItemWithBillable.encounterItem.quantity
            )

    data class BillableApi(
            @SerializedName("id") val id: UUID,
            @SerializedName("type") val type: Billable.Type,
            @SerializedName("composition") val composition: String?,
            @SerializedName("unit") val unit: String?,
            @SerializedName("price") val price: Int,
            @SerializedName("name") val name: String
    )
}
