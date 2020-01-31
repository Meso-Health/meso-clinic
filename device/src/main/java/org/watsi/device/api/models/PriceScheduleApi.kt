package org.watsi.device.api.models

import org.threeten.bp.Instant
import org.watsi.domain.entities.PriceSchedule
import java.util.UUID

data class PriceScheduleApi(
    val id: UUID,
    val issuedAt: Instant,
    val billableId: UUID,
    val price: Int,
    val previousPriceScheduleId: UUID?
) {

    constructor (priceSchedule: PriceSchedule) : this(
        id = priceSchedule.id,
        issuedAt = priceSchedule.issuedAt,
        billableId = priceSchedule.billableId,
        price = priceSchedule.price,
        previousPriceScheduleId = priceSchedule.previousPriceScheduleModelId
    )

    fun toPriceSchedule(): PriceSchedule {
        return PriceSchedule(
            id = id,
            issuedAt = issuedAt,
            billableId = billableId,
            price = price,
            previousPriceScheduleModelId = previousPriceScheduleId
        )
    }
}
