package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.relations.BillableWithPriceSchedule
import java.util.UUID

data class BillableWithPriceScheduleApi(
    @SerializedName("id") val id: UUID,
    @SerializedName("type") val type: String,
    @SerializedName("composition") val composition: String?,
    @SerializedName("unit") val unit: String?,
    @SerializedName("price") val price: Int,
    @SerializedName("name") val name: String,
    @SerializedName("active_price_schedule") val priceScheduleApi: PriceScheduleApi
) {
    constructor (billable: Billable, activePriceSchedule: PriceSchedule) : this(
        id = billable.id,
        type = billable.type.toString().toLowerCase(),
        composition = billable.composition?.toLowerCase(),
        unit = billable.unit,
        price = billable.price,
        name = billable.name,
        priceScheduleApi = PriceScheduleApi(
            id = activePriceSchedule.id,
            issuedAt = activePriceSchedule.issuedAt,
            billableId = activePriceSchedule.billableId,
            price = activePriceSchedule.price,
            previousPriceScheduleId = activePriceSchedule.previousPriceScheduleModelId
        )
    )

    fun toBillableWithPriceSchedule(): BillableWithPriceSchedule {
        return BillableWithPriceSchedule(
            billable = Billable(
                id = id,
                type = Billable.Type.valueOf(type.toUpperCase()),
                composition = composition,
                unit = unit,
                price = price,
                name = name
            ),
            priceSchedule = priceScheduleApi.toPriceSchedule()
        )
    }
}
