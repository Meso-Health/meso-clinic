package org.watsi.domain.relations

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule
import java.io.Serializable

data class BillableWithPriceSchedules(
    val billable: Billable,
    val priceScheduleList: List<PriceSchedule>
) : Serializable {
    fun toCurrentBillableWithPrice(): BillableWithPriceSchedule {
        val currentPriceSchedule = priceScheduleList.maxBy { it.issuedAt }
                ?: throw IllegalStateException("Billable Missing Corresponding Price Schedule")
        val prevPriceSchedule = priceScheduleList.find { it.id == currentPriceSchedule.previousPriceScheduleModelId }

        return BillableWithPriceSchedule(billable, currentPriceSchedule, prevPriceSchedule)
    }
}
