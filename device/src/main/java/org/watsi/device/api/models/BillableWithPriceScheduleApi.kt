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
    val name: String,
    val active: Boolean,
    val requiresLabResult: Boolean,
    val activePriceSchedule: PriceScheduleApi
) {
    constructor (billable: Billable, activePriceSchedule: PriceSchedule) : this(
        id = billable.id,
        type = billable.type.toString().toLowerCase(),
        composition = billable.composition?.toLowerCase(),
        unit = billable.unit,
        name = billable.name,
        active = billable.active,
        requiresLabResult = billable.requiresLabResult,
        activePriceSchedule = PriceScheduleApi(
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
                name = name,
                active = active,
                requiresLabResult = requiresLabResult
            ),
            priceSchedule = activePriceSchedule.toPriceSchedule()
        )
    }
}
