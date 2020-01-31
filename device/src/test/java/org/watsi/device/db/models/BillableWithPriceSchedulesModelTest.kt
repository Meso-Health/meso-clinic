package org.watsi.device.db.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.PriceScheduleModelFactory
import org.watsi.domain.relations.BillableWithPriceSchedule

class BillableWithPriceSchedulesModelTest {

    @Test
    fun toBillableWithCurrentPriceSchedule() {
        val oldDate = Instant.ofEpochMilli(1533090767000) // 2018/08/01
        val middleDate = Instant.ofEpochMilli(1534300367000) // 2018/08/15
        val newDate = Instant.ofEpochMilli(1535769167000) // 2018/09/01

        val billable = BillableModelFactory.build()
        val priceSchedule1 = PriceScheduleModelFactory.build(billableId = billable.id, issuedAt = oldDate)
        val priceSchedule2 = PriceScheduleModelFactory.build(billableId = billable.id, issuedAt = middleDate, previousPriceScheduleModelId = priceSchedule1.id)
        val priceSchedule3 = PriceScheduleModelFactory.build(billableId = billable.id, issuedAt = newDate, previousPriceScheduleModelId = priceSchedule1.id)
        val billableWithPriceSchedules = BillableWithPriceSchedulesModel(billable, listOf(priceSchedule1, priceSchedule2, priceSchedule3))

        assertEquals(BillableWithPriceSchedule(billable.toBillable(), priceSchedule3.toPriceSchedule()), billableWithPriceSchedules.toBillableWithCurrentPriceSchedule())
    }
}
