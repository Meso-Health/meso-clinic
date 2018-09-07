package org.watsi.domain.relations

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.PriceScheduleFactory

class EncounterItemWithBillableAndPriceTest {

    @Test
    fun price() {
        val encounterItem = EncounterItemFactory.build(quantity = 2)
        val billable = BillableFactory.build()
        val priceSchedule = PriceScheduleFactory.build(billableId = billable.id, price = 150)
        val billableWithPriceSchedule =
            BillableWithPriceScheduleFactory.build(billable, priceSchedule)
        val encounterItemRelation =
            EncounterItemWithBillableAndPrice(encounterItem, billableWithPriceSchedule)

        assertEquals(300, encounterItemRelation.price())
    }
}
