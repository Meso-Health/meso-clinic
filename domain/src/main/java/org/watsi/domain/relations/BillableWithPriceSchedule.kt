package org.watsi.domain.relations

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule

data class BillableWithPriceSchedule(
    val billable: Billable,
    val priceSchedule: PriceSchedule,
    val prevPriceSchedule: PriceSchedule? = null
) {
    fun priceSchedules(): List<PriceSchedule> {
        return if (prevPriceSchedule != null) listOf(priceSchedule, prevPriceSchedule) else listOf(priceSchedule)
    }
}
