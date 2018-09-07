package org.watsi.domain.relations

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.PriceScheduleFactory

class BillableWithPriceScheduleListTest {

    @Test
    fun toCurrentBillableWithPrice() {
        val oldDate = Instant.ofEpochMilli(1533090767000) // 2018/08/01
        val middleDate = Instant.ofEpochMilli(1534300367000) // 2018/08/15
        val newDate = Instant.ofEpochMilli(1535769167000) // 2018/09/01

        val billable = BillableFactory.build()
        val priceSchedule1 = PriceScheduleFactory.build(billableId = billable.id, issuedAt = oldDate)
        val priceSchedule2 = PriceScheduleFactory.build(billableId = billable.id, issuedAt = middleDate, previousPriceScheduleModelId = priceSchedule1.id)
        val priceSchedule3 = PriceScheduleFactory.build(billableId = billable.id, issuedAt = newDate, previousPriceScheduleModelId = priceSchedule1.id)
        val billableWithPriceScheduleList = BillableWithPriceScheduleList(billable, listOf(priceSchedule1, priceSchedule2, priceSchedule3))

        assertEquals(BillableWithPriceSchedule(billable, priceSchedule3), billableWithPriceScheduleList.toCurrentBillableWithPrice())
    }
}
