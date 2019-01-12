package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant
import org.watsi.domain.entities.PriceSchedule
import java.util.UUID

data class PriceScheduleApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("issued_at") val issuedAt: Instant,
        @SerializedName("billable_id") val billableId: UUID,
        @SerializedName("price") val price: Int,
        @SerializedName("previous_price_schedule_id") val previousPriceScheduleId: UUID?
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
