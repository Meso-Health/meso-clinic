package org.watsi.domain.factories

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.relations.BillableWithPriceSchedule

object BillableWithPriceScheduleFactory {
    fun build(
        billable: Billable = BillableFactory.build(),
        priceSchedule: PriceSchedule = PriceScheduleFactory.build(billableId = billable.id),
        prevPriceSchedule: PriceSchedule? = null
    ): BillableWithPriceSchedule {
        return BillableWithPriceSchedule(billable, priceSchedule, prevPriceSchedule)
    }
}
