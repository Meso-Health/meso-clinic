package org.watsi.domain.factories

import org.threeten.bp.Instant
import org.watsi.domain.entities.PriceSchedule
import java.util.UUID


object PriceScheduleFactory {
    fun build(
        id: UUID = UUID.randomUUID(),
        issuedAt: Instant = Instant.now(),
        billableId: UUID = UUID.randomUUID(),
        price: Int = 10,
        previousPriceScheduleModelId: UUID? = null
    ) : PriceSchedule {
        return PriceSchedule(
            id = id,
            issuedAt = issuedAt,
            billableId = billableId,
            price = price,
            previousPriceScheduleModelId = previousPriceScheduleModelId
        )
    }
}
