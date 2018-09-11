package org.watsi.domain.relations

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule

data class BillableWithPriceSchedule(
    val billable: Billable,
    val priceSchedule: PriceSchedule,
    val prevPriceSchedule: PriceSchedule? = null
)
