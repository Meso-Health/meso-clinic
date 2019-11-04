package org.watsi.domain.relations

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.PriceScheduleFactory

class EncounterWithExtrasTest {

    @Test
    fun price_noStockouts() {
        val encounterItem1 = EncounterItemFactory.build(quantity = 1)
        val encounterItem2 = EncounterItemFactory.build(quantity = 2)
        val encounterItem3 = EncounterItemFactory.build(quantity = 3)
        val billable1 = BillableFactory.build()
        val billable2 = BillableFactory.build()
        val billable3 = BillableFactory.build()
        val priceSchedule1 = PriceScheduleFactory.build(billableId = billable1.id, price = 10)
        val priceSchedule2 = PriceScheduleFactory.build(billableId = billable2.id, price = 25)
        val priceSchedule3 = PriceScheduleFactory.build(billableId = billable3.id, price = 100)
        val billableWithPriceSchedule1 =
            BillableWithPriceScheduleFactory.build(billable1, priceSchedule1)
        val billableWithPriceSchedule2 =
            BillableWithPriceScheduleFactory.build(billable2, priceSchedule2)
        val billableWithPriceSchedule3 =
            BillableWithPriceScheduleFactory.build(billable3, priceSchedule3)


        val encounterRelationList = listOf(
            EncounterItemWithBillableAndPrice(encounterItem1, billableWithPriceSchedule1, null),
            EncounterItemWithBillableAndPrice(encounterItem2, billableWithPriceSchedule2, null),
            EncounterItemWithBillableAndPrice(encounterItem3, billableWithPriceSchedule3, null)
        )

        assertEquals(360, EncounterWithExtras.price(encounterRelationList))
    }

    @Test
    fun price_withStockouts() {
        val encounterItem1 = EncounterItemFactory.build(quantity = 1, stockout = false)
        val encounterItem2 = EncounterItemFactory.build(quantity = 2, stockout = false)
        val encounterItem3 = EncounterItemFactory.build(quantity = 3, stockout = true)
        val billable1 = BillableFactory.build()
        val billable2 = BillableFactory.build()
        val priceSchedule1 = PriceScheduleFactory.build(billableId = billable1.id, price = 10)
        val priceSchedule2 = PriceScheduleFactory.build(billableId = billable2.id, price = 25)
        val billableWithPriceSchedule1 =
            BillableWithPriceScheduleFactory.build(billable1, priceSchedule1)
        val billableWithPriceSchedule2 =
            BillableWithPriceScheduleFactory.build(billable2, priceSchedule2)


        val encounterRelationList = listOf(
            EncounterItemWithBillableAndPrice(encounterItem1, billableWithPriceSchedule1, null),
            EncounterItemWithBillableAndPrice(encounterItem2, billableWithPriceSchedule2, null),
            EncounterItemWithBillableAndPrice(encounterItem3, billableWithPriceSchedule2, null)
        )

        assertEquals(60, EncounterWithExtras.price(encounterRelationList))
    }
}
