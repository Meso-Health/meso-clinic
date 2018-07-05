package org.watsi.domain.relations

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.EncounterItemFactory

class EncounterItemWithBillableTest {

    @Test
    fun price() {
        val encounterItem = EncounterItemFactory.build(quantity = 2)
        val billable = BillableFactory.build(price = 150)
        val encounterItemWithBillable = EncounterItemWithBillable(encounterItem, billable)

        assertEquals(300, encounterItemWithBillable.price())
    }
}
