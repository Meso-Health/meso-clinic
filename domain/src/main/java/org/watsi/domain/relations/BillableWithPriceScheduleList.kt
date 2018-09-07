package org.watsi.domain.relations

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule
import java.io.Serializable

data class BillableWithPriceScheduleList(
    val billable: Billable,
    val priceScheduleList: List<PriceSchedule>
) : Serializable {
    fun toCurrentBillableWithPrice(): BillableWithPriceSchedule {
        return BillableWithPriceSchedule(
            billable,
            priceScheduleList.maxBy { it.issuedAt }
                ?: throw IllegalStateException("Billable Missing Corresponding Price Schedule"))
    }
}
