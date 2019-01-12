package org.watsi.domain.relations

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule
import java.io.Serializable

data class BillableWithPriceSchedule(
    val billable: Billable,
    val priceSchedule: PriceSchedule,
    val prevPriceSchedule: PriceSchedule? = null
): Serializable {
    fun priceSchedules(): List<PriceSchedule> {
        return listOf(priceSchedule, prevPriceSchedule).filterNotNull()
    }
}
