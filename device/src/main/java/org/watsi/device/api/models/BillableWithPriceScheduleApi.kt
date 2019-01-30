package org.watsi.device.api.models

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.relations.BillableWithPriceSchedule
import java.util.UUID

data class BillableWithPriceScheduleApi(
    val id: UUID,
    val type: String,
    val composition: String?,
    val unit: String?,
    val price: Int,
    val name: String,
    val priceScheduleApi: PriceScheduleApi
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
