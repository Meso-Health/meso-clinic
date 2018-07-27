package org.watsi.uhp.flowstates

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.domain.entities.Billable
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterItemWithBillableFactory
import org.watsi.domain.relations.EncounterItemWithBillable

class EncounterWithItemsAndFormsTest {

    @Test
    fun price() {
        val encounterItem1 = EncounterItemFactory.build(quantity = 2)
        val billable1 = BillableFactory.build(price = 150)
        val encounterItemWithBillable1 = EncounterItemWithBillable(encounterItem1, billable1)

        val encounterItem2 = EncounterItemFactory.build(quantity = 4)
        val billable2 = BillableFactory.build(price = 50)
        val encounterItemWithBillable2 = EncounterItemWithBillable(encounterItem2, billable2)

        val encounter = EncounterFactory.build()
        val encounterWithItemsAndForms = EncounterFlowState(
                encounter, listOf(encounterItemWithBillable1, encounterItemWithBillable2),
                emptyList(), emptyList())

        assertEquals(500, encounterWithItemsAndForms.price())
    }

    @Test
    fun getEncounterItemsOfType() {
        val drugEncounterItem = EncounterItemWithBillableFactory.build(
                billable = BillableFactory.build(type = Billable.Type.DRUG))
        val serviceEncounterItem = EncounterItemWithBillableFactory.build(
                billable = BillableFactory.build(type = Billable.Type.SERVICE))
        val encounterFlowState = EncounterFlowState(EncounterFactory.build(),
                listOf(drugEncounterItem, serviceEncounterItem), emptyList(), emptyList())

        assertEquals(listOf(drugEncounterItem), encounterFlowState.getEncounterItemsOfType(Billable.Type.DRUG))
    }
}
