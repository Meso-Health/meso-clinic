package org.watsi.domain.relations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
            EncounterItemWithBillableAndPrice(
                encounterItem = encounterItem,
                billableWithPriceSchedule = billableWithPriceSchedule,
                labResult = null
            )

        assertEquals(300, encounterItemRelation.price())
    }

    @Test
    fun prevPrice_noPrevPriceSchedule() {
        val encounterItem = EncounterItemFactory.build(quantity = 2)
        val billable = BillableFactory.build()
        val priceSchedule = PriceScheduleFactory.build(billableId = billable.id, price = 150)
        val billableWithPriceSchedule =
                BillableWithPriceScheduleFactory.build(billable, priceSchedule, null)
        val encounterItemRelation =
                EncounterItemWithBillableAndPrice(
                    encounterItem = encounterItem,
                    billableWithPriceSchedule = billableWithPriceSchedule,
                    labResult = null
                )

        assertNull(encounterItemRelation.prevPrice())
    }

    @Test
    fun prevPrice_withPrevPriceSchedule() {
        val encounterItem = EncounterItemFactory.build(quantity = 2)
        val billable = BillableFactory.build()
        val priceSchedule = PriceScheduleFactory.build(billableId = billable.id, price = 150)
        val prevPriceSchedule = PriceScheduleFactory.build(billableId = billable.id, price = 100)
        val billableWithPriceSchedule =
                BillableWithPriceScheduleFactory.build(billable, priceSchedule, prevPriceSchedule)
        val encounterItemRelation =
                EncounterItemWithBillableAndPrice(
                    encounterItem = encounterItem,
                    billableWithPriceSchedule = billableWithPriceSchedule,
                    labResult = null
                )

        assertEquals(200, encounterItemRelation.prevPrice())
    }
}
